package com.areswayne.flowdesk.domain.notification;

import com.areswayne.flowdesk.domain.notification.event.NotificationRequestedEvent;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.shared.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @Test
    void persistsAndBroadcastsNotificationToAuthenticatedUserDestination() {
        UUID recipientId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        User recipient = User.builder().id(recipientId).email("user@flowdesk.test").build();
        NotificationService service = new NotificationService(
                notificationRepository, userRepository, messagingTemplate);

        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(UUID.randomUUID());
            return notification;
        });

        service.createFromEvent(new NotificationRequestedEvent(
                recipientId, NotificationType.TASK_ASSIGNED, "Nueva tarea",
                "Se te asignó una tarea", "TASK", taskId, null
        ));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getId()).isEqualTo(recipientId);
        assertThat(captor.getValue().isRead()).isFalse();
        verify(messagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("user@flowdesk.test"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"),
                any()
        );
    }
}
