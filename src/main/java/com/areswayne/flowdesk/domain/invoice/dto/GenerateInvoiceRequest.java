package com.areswayne.flowdesk.domain.invoice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GenerateInvoiceRequest(

        @NotNull(message = "El projectId es obligatorio")
        UUID projectId,

        // Si es null, usa todas las horas no facturadas del proyecto
        LocalDate fromDate,
        LocalDate toDate,

        // Días para vencimiento — default 30
        Integer dueDays
) {}