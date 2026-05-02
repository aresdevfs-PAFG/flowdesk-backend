package com.areswayne.flowdesk.domain.task.dto;

import com.areswayne.flowdesk.shared.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        TaskStatus status
) {}