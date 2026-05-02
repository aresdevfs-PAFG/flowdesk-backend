package com.areswayne.flowdesk.domain.project.dto;

import com.areswayne.flowdesk.shared.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String description,
        ProjectStatus status,
        BigDecimal hourlyRate,
        LocalDate startDate,
        LocalDate endDate,
        int memberCount,
        long taskCount,
        LocalDateTime createdAt
) {}