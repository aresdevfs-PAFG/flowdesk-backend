package com.areswayne.flowdesk.domain.notification;

import com.areswayne.flowdesk.domain.notification.dto.NotificationResponse;
import com.areswayne.flowdesk.domain.notification.dto.UnreadCountResponse;
import com.areswayne.flowdesk.domain.notification.event.NotificationRequestedEvent;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMine(boolean unreadOnly) {
        UUID userId = getCurrentUser().getId();
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        return new UnreadCountResponse(
                notificationRepository.countByUserIdAndReadFalse(getCurrentUser().getId())
        );
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID userId = getCurrentUser().getId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead(getCurrentUser().getId());
    }

    @Transactional
    public void createFromEvent(NotificationRequestedEvent event) {
        if (event.eventKey() != null && notificationRepository.existsByEventKey(event.eventKey())) {
            return;
        }

        User recipient = userRepository.findById(event.recipientId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario destinatario no encontrado"));

        Notification notification = Notification.builder()
                .user(recipient)
                .type(event.type())
                .title(event.title())
                .message(event.message())
                .resourceType(event.resourceType())
                .resourceId(event.resourceId())
                .eventKey(event.eventKey())
                .read(false)
                .build();

        NotificationResponse response = toResponse(notificationRepository.save(notification));
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(), "/queue/notifications", response
        );
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getResourceType(), notification.getResourceId(),
                notification.isRead(), notification.getCreatedAt()
        );
    }
}
