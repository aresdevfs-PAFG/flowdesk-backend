package com.areswayne.flowdesk.domain.notification.dto;

import com.areswayne.flowdesk.shared.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        String resourceType,
        UUID resourceId,
        boolean read,
        LocalDateTime createdAt
) {}
