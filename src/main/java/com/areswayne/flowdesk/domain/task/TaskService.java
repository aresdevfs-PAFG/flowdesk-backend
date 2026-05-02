package com.areswayne.flowdesk.domain.task;

import com.areswayne.flowdesk.domain.project.Project;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

    @Transactional
    public TaskResponse create(UUID projectId, TaskRequest request) {
        User current = getCurrentUser();
        validateProjectAccess(projectId, current.getId());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        // La nueva tarea va al final del tablero
        int nextPosition = taskRepository.findMaxPositionByProjectId(projectId) + 1;

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario asignado no encontrado"));
        }

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

        return toResponse(taskRepository.save(task));
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

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario asignado no encontrado"));
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : task.getPriority());
        task.setAssignee(assignee);
        task.setDueDate(request.dueDate());

        return toResponse(taskRepository.save(task));
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