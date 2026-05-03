package com.areswayne.flowdesk.domain.timeentry.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ManualEntryRequest(

        @NotNull(message = "El taskId es obligatorio")
        UUID taskId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime startedAt,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime endedAt,

        String note
) {}