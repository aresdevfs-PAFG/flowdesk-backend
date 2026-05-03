package com.areswayne.flowdesk.domain.comment;

import com.areswayne.flowdesk.domain.comment.dto.CommentRequest;
import com.areswayne.flowdesk.domain.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ── REST endpoints ────────────────────────────────────────

    // Cargar historial de comentarios al abrir una tarea
    @GetMapping("/api/tasks/{taskId}/comments")
    public ResponseEntity<List<CommentResponse>> getByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(commentService.getByTask(taskId));
    }

    // Crear comentario vía REST (también hace broadcast WebSocket internamente)
    @PostMapping("/api/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID taskId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(taskId, request));
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.update(commentId, request));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }

    // ── WebSocket endpoint ────────────────────────────────────

    // El cliente puede enviar comentarios directamente por WebSocket
    // Destino: /app/tasks/{taskId}/comments
    @MessageMapping("/tasks/{taskId}/comments")
    public void handleWebSocketComment(
            @DestinationVariable UUID taskId,
            CommentRequest request) {
        commentService.create(taskId, request);
    }
}