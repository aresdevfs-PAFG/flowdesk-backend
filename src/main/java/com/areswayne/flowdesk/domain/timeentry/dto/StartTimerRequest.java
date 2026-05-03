package com.areswayne.flowdesk.domain.timeentry.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartTimerRequest(
        @NotNull(message = "El taskId es obligatorio")
        UUID taskId
) {}