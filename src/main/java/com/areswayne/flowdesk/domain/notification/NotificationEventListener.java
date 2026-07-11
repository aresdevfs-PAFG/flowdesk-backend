package com.areswayne.flowdesk.domain.notification;

import com.areswayne.flowdesk.domain.notification.event.NotificationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationRequested(NotificationRequestedEvent event) {
        notificationService.createFromEvent(event);
    }
}
