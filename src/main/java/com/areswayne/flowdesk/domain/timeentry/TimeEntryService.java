package com.areswayne.flowdesk.domain.timeentry;

import com.areswayne.flowdesk.domain.task.Task;
import com.areswayne.flowdesk.domain.task.TaskRepository;
import com.areswayne.flowdesk.domain.timeentry.dto.ManualEntryRequest;
import com.areswayne.flowdesk.domain.timeentry.dto.StartTimerRequest;
import com.areswayne.flowdesk.domain.timeentry.dto.TimeEntryResponse;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TimeEntryResponse startTimer(StartTimerRequest request) {
        User current = getCurrentUser();

        // Un usuario solo puede tener un timer activo a la vez
        timeEntryRepository.findByUserIdAndEndedAtIsNull(current.getId())
                .ifPresent(active -> {
                    throw new IllegalArgumentException(
                        "Ya tienes un timer activo. Detenlo antes de iniciar uno nuevo.");
                });

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .user(current)
                .startedAt(LocalDateTime.now())
                .minutes(0)
                .build();

        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponse stopTimer() {
        User current = getCurrentUser();

        TimeEntry entry = timeEntryRepository
                .findByUserIdAndEndedAtIsNull(current.getId())
                .orElseThrow(() -> new IllegalArgumentException("No tienes ningún timer activo"));

        LocalDateTime now = LocalDateTime.now();
        int minutes = (int) ChronoUnit.MINUTES.between(entry.getStartedAt(), now);

        entry.setEndedAt(now);
        entry.setMinutes(Math.max(minutes, 1)); // mínimo 1 minuto

        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponse addManual(ManualEntryRequest request) {
        User current = getCurrentUser();

        if (request.endedAt().isBefore(request.startedAt())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        int minutes = (int) ChronoUnit.MINUTES.between(request.startedAt(), request.endedAt());

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .user(current)
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .minutes(Math.max(minutes, 1))
                .note(request.note())
                .build();

        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getByTask(UUID taskId) {
        return timeEntryRepository.findByTaskIdOrderByStartedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getMyEntries() {
        User current = getCurrentUser();
        return timeEntryRepository.findByUserIdOrderByStartedAtDesc(current.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID entryId) {
        User current = getCurrentUser();
        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada"));

        if (!entry.getUser().getId().equals(current.getId())) {
            throw new SecurityException("Solo puedes eliminar tus propias entradas");
        }

        timeEntryRepository.deleteById(entryId);
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private TimeEntryResponse toResponse(TimeEntry e) {
        return new TimeEntryResponse(
                e.getId(),
                e.getTask().getId(),
                e.getTask().getTitle(),
                e.getUser().getId(),
                e.getUser().getName(),
                e.getStartedAt(),
                e.getEndedAt(),
                e.getMinutes(),
                e.getNote(),
                e.getEndedAt() == null
        );
    }
}