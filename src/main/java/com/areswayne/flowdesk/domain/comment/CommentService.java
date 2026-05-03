package com.areswayne.flowdesk.domain.comment;

import com.areswayne.flowdesk.domain.comment.dto.CommentRequest;
import com.areswayne.flowdesk.domain.comment.dto.CommentResponse;
import com.areswayne.flowdesk.domain.task.Task;
import com.areswayne.flowdesk.domain.task.TaskRepository;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public CommentResponse create(UUID taskId, CommentRequest request) {
        return createComment(taskId, request, getCurrentUser());
    }

    @Transactional
    public CommentResponse create(UUID taskId, CommentRequest request, String email) {
        User current = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return createComment(taskId, request, current);
    }

    private CommentResponse createComment(UUID taskId, CommentRequest request, User author) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        Comment comment = Comment.builder()
                .task(task)
                .author(author)
                .body(request.body())
                .build();

        Comment saved = commentRepository.save(comment);
        CommentResponse response = toResponse(saved);

        // Broadcast a todos los clientes suscritos a esta tarea
        messagingTemplate.convertAndSend(
                "/topic/tasks/" + taskId + "/comments",
                response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getByTask(UUID taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse update(UUID commentId, CommentRequest request) {
        User current = getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado"));

        if (!comment.getAuthor().getId().equals(current.getId())) {
            throw new SecurityException("Solo puedes editar tus propios comentarios");
        }

        comment.setBody(request.body());
        Comment saved = commentRepository.save(comment);
        CommentResponse response = toResponse(saved);

        // Notifica la edición en tiempo real
        messagingTemplate.convertAndSend(
                "/topic/tasks/" + comment.getTask().getId() + "/comments/updates",
                response
        );

        return response;
    }

    @Transactional
    public void delete(UUID commentId) {
        User current = getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado"));

        if (!comment.getAuthor().getId().equals(current.getId())) {
            throw new SecurityException("Solo puedes eliminar tus propios comentarios");
        }

        UUID taskId = comment.getTask().getId();
        commentRepository.deleteById(commentId);

        // Notifica la eliminación en tiempo real
        messagingTemplate.convertAndSend(
                "/topic/tasks/" + taskId + "/comments/deleted",
                commentId
        );
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getTask().getId(),
                c.getAuthor().getId(),
                c.getAuthor().getName(),
                c.getAuthor().getAvatarUrl(),
                c.getBody(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}