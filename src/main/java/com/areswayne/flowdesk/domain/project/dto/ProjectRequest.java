package com.areswayne.flowdesk.domain.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,

        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
        BigDecimal hourlyRate,

        LocalDate startDate,
        LocalDate endDate
) {}