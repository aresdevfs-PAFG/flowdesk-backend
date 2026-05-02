package com.areswayne.flowdesk.domain.task.dto;

import com.areswayne.flowdesk.shared.enums.Priority;
import com.areswayne.flowdesk.shared.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        int position,
        LocalDate dueDate,
        AssigneeInfo assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // Objeto anidado para el asignado — no exponemos el UUID crudo
    public record AssigneeInfo(UUID id, String name, String avatarUrl) {}
}