package com.areswayne.flowdesk.domain.task.dto;

import com.areswayne.flowdesk.shared.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200)
        String title,

        String description,

        Priority priority,

        UUID assigneeId,

        LocalDate dueDate
) {}