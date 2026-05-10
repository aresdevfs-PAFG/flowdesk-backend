package com.areswayne.flowdesk.domain.invoice.dto;

import com.areswayne.flowdesk.shared.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInvoiceStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        InvoiceStatus status
) {}