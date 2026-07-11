package com.areswayne.flowdesk.domain.report.dto;

import com.areswayne.flowdesk.shared.enums.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProjectReportResponse(
        UUID projectId,
        String projectName,
        LocalDate fromDate,
        LocalDate toDate,
        int totalMinutes,
        BigDecimal totalHours,
        BigDecimal billableAmount,
        long totalTasks,
        long completedTasks,
        long pendingTasks,
        BigDecimal completionPercentage,
        Map<TaskStatus, Long> tasksByStatus,
        List<UserHours> hoursByUser
) {
    public record UserHours(
            UUID userId,
            String userName,
            int minutes,
            BigDecimal hours,
            BigDecimal billableAmount
    ) {}
}
