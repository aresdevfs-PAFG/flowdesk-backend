package com.areswayne.flowdesk.domain.notification.event;

import com.areswayne.flowdesk.shared.enums.NotificationType;

import java.util.UUID;

public record NotificationRequestedEvent(
        UUID recipientId,
        NotificationType type,
        String title,
        String message,
        String resourceType,
        UUID resourceId,
        String eventKey
) {}
