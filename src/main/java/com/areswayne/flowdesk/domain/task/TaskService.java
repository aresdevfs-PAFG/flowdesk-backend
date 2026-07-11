package com.areswayne.flowdesk.domain.task;

import com.areswayne.flowdesk.domain.project.Project;
import com.areswayne.flowdesk.domain.notification.event.NotificationRequestedEvent;
import com.areswayne.flowdesk.domain.project.ProjectMemberRepository;
import com.areswayne.flowdesk.domain.project.ProjectRepository;
import com.areswayne.flowdesk.domain.task.dto.ReorderRequest;
import com.areswayne.flowdesk.domain.task.dto.TaskRequest;
import com.areswayne.flowdesk.domain.task.dto.TaskResponse;
import com.areswayne.flowdesk.domain.task.dto.UpdateStatusRequest;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.shared.enums.Priority;
import com.areswayne.flowdesk.shared.enums.TaskStatus;
import com.areswayne.flowdesk.shared.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TaskResponse create(UUID projectId, TaskRequest request) {
        User current = getCurrentUser();
        validateProjectAccess(projectId, current.getId());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        // La nueva tarea va al final del tablero
        int nextPosition = taskRepository.findMaxPositionByProjectId(projectId) + 1;

        User assignee = findValidAssignee(projectId, request.assigneeId());

        Task task = Task.builder()
                .project(project)
                .assignee(assignee)
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .priority(request.priority() != null ? request.priority() : Priority.MEDIUM)
                .position(nextPosition)
                .dueDate(request.dueDate())
                .build();

        Task saved = taskRepository.save(task);
        publishAssignmentNotification(saved, current);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByProject(UUID projectId) {
        User current = getCurrentUser();
        validateProjectAccess(projectId, current.getId());
        return taskRepository.findByProjectIdOrderByPosition(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByProjectAndStatus(UUID projectId, TaskStatus status) {
        User current = getCurrentUser();
        validateProjectAccess(projectId, current.getId());
        return taskRepository.findByProjectIdAndStatusOrderByPosition(projectId, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(UUID taskId) {
        Task task = findAndValidateAccess(taskId);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse update(UUID taskId, TaskRequest request) {
        Task task = findAndValidateAccess(taskId);
        User current = getCurrentUser();
        UUID previousAssigneeId = task.getAssignee() == null ? null : task.getAssignee().getId();

        User assignee = findValidAssignee(task.getProject().getId(), request.assigneeId());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : task.getPriority());
        task.setAssignee(assignee);
        task.setDueDate(request.dueDate());

        Task saved = taskRepository.save(task);
        UUID newAssigneeId = assignee == null ? null : assignee.getId();
        if (!java.util.Objects.equals(previousAssigneeId, newAssigneeId)) {
            publishAssignmentNotification(saved, current);
        }
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateStatus(UUID taskId, UpdateStatusRequest request) {
        Task task = findAndValidateAccess(taskId);
        task.setStatus(request.status());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse reorder(UUID taskId, ReorderRequest request) {
        Task task = findAndValidateAccess(taskId);

        List<Task> tasks = taskRepository
                .findByProjectIdOrderByPosition(task.getProject().getId());

        int oldPosition = task.getPosition();
        int newPosition = request.newPosition();

        // Reajusta posiciones de las demás tareas
        for (Task t : tasks) {
            if (t.getId().equals(taskId)) continue;

            if (oldPosition < newPosition) {
                if (t.getPosition() > oldPosition && t.getPosition() <= newPosition) {
                    t.setPosition(t.getPosition() - 1);
                }
            } else {
                if (t.getPosition() >= newPosition && t.getPosition() < oldPosition) {
                    t.setPosition(t.getPosition() + 1);
                }
            }
        }

        task.setPosition(newPosition);
        taskRepository.saveAll(tasks);

        return toResponse(task);
    }

    @Transactional
    public void delete(UUID taskId) {
        findAndValidateAccess(taskId);
        taskRepository.deleteById(taskId);
    }

    // ── Helpers ──────────────────────────────────────────────

    private void validateProjectAccess(UUID projectId, UUID userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new SecurityException("No tienes acceso a este proyecto");
        }
    }

    private Task findAndValidateAccess(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        User current = getCurrentUser();
        validateProjectAccess(task.getProject().getId(), current.getId());
        return task;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private User findValidAssignee(UUID projectId, UUID assigneeId) {
        if (assigneeId == null) return null;
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
            throw new IllegalArgumentException("El usuario asignado debe pertenecer al proyecto");
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario asignado no encontrado"));
    }

    private void publishAssignmentNotification(Task task, User actor) {
        if (task.getAssignee() == null || task.getAssignee().getId().equals(actor.getId())) return;
        eventPublisher.publishEvent(new NotificationRequestedEvent(
                task.getAssignee().getId(), NotificationType.TASK_ASSIGNED,
                "Nueva tarea asignada",
                actor.getName() + " te asignó la tarea \"" + task.getTitle() + "\"",
                "TASK", task.getId(), null
        ));
    }

    private TaskResponse toResponse(Task t) {
        TaskResponse.AssigneeInfo assigneeInfo = null;
        if (t.getAssignee() != null) {
            assigneeInfo = new TaskResponse.AssigneeInfo(
                    t.getAssignee().getId(),
                    t.getAssignee().getName(),
                    t.getAssignee().getAvatarUrl()
            );
        }

        return new TaskResponse(
                t.getId(),
                t.getProject().getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getPosition(),
                t.getDueDate(),
                assigneeInfo,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
