package com.areswayne.flowdesk.domain.notification;

import com.areswayne.flowdesk.domain.notification.event.NotificationRequestedEvent;
import com.areswayne.flowdesk.domain.task.TaskRepository;
import com.areswayne.flowdesk.shared.enums.NotificationType;
import com.areswayne.flowdesk.shared.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DueDateNotificationScheduler {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${app.notifications.due-date-cron:0 0 8 * * *}",
            zone = "${app.notifications.time-zone:America/Mexico_City}")
    @Transactional(readOnly = true)
    public void notifyUpcomingDueDates() {
        LocalDate today = LocalDate.now();
        taskRepository.findByDueDateBetweenAndAssigneeIsNotNullAndStatusNotIn(
                today, today.plusDays(1), List.of(TaskStatus.DONE, TaskStatus.CANCELLED)
        ).forEach(task -> eventPublisher.publishEvent(new NotificationRequestedEvent(
                task.getAssignee().getId(), NotificationType.DUE_DATE_UPCOMING,
                "Fecha límite próxima",
                "La tarea \"" + task.getTitle() + "\" vence el " + task.getDueDate(),
                "TASK", task.getId(),
                "due-date:" + task.getId() + ":" + task.getDueDate()
        )));
    }
}
