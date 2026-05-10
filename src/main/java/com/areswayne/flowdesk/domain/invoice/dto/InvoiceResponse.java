package com.areswayne.flowdesk.domain.invoice.dto;

import com.areswayne.flowdesk.shared.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String number,
        BigDecimal total,
        InvoiceStatus status,
        LocalDate issuedAt,
        LocalDate dueAt,
        List<InvoiceItemResponse> items,
        LocalDateTime createdAt
) {}