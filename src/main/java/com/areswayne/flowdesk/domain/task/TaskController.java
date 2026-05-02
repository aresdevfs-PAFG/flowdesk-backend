package com.areswayne.flowdesk.domain.task;

import com.areswayne.flowdesk.domain.task.dto.ReorderRequest;
import com.areswayne.flowdesk.domain.task.dto.TaskRequest;
import com.areswayne.flowdesk.domain.task.dto.TaskResponse;
import com.areswayne.flowdesk.domain.task.dto.UpdateStatusRequest;
import com.areswayne.flowdesk.shared.enums.TaskStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return ResponseEntity.ok(taskService.getByProjectAndStatus(projectId, status));
        }
        return ResponseEntity.ok(taskService.getByProject(projectId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getById(taskId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(taskId, request));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(taskId, request));
    }

    @PatchMapping("/{taskId}/reorder")
    public ResponseEntity<TaskResponse> reorder(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody ReorderRequest request) {
        return ResponseEntity.ok(taskService.reorder(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        taskService.delete(taskId);
        return ResponseEntity.noContent().build();
    }
}