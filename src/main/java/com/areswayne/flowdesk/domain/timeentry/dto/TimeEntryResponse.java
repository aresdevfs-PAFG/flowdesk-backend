package com.areswayne.flowdesk.domain.timeentry.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimeEntryResponse(
        UUID id,
        UUID taskId,
        String taskTitle,
        UUID userId,
        String userName,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        int minutes,
        String note,
        boolean active
) {}