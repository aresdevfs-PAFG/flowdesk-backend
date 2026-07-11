package com.areswayne.flowdesk.domain.report;

import com.areswayne.flowdesk.domain.project.Project;
import com.areswayne.flowdesk.domain.project.ProjectMemberRepository;
import com.areswayne.flowdesk.domain.project.ProjectRepository;
import com.areswayne.flowdesk.domain.report.dto.ProjectReportResponse;
import com.areswayne.flowdesk.domain.task.TaskRepository;
import com.areswayne.flowdesk.domain.timeentry.TimeEntry;
import com.areswayne.flowdesk.domain.timeentry.TimeEntryRepository;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.shared.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProjectReportResponse getProjectSummary(UUID projectId, LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        Project project = findAndValidateAccess(projectId);

        var tasks = taskRepository.findByProjectIdOrderByPosition(projectId);
        List<TimeEntry> entries = timeEntryRepository.findByProjectId(projectId, fromDate, toDate);

        Map<TaskStatus, Long> tasksByStatus = new EnumMap<>(TaskStatus.class);
        Arrays.stream(TaskStatus.values()).forEach(status -> tasksByStatus.put(status, 0L));
        tasks.forEach(task -> tasksByStatus.compute(task.getStatus(), (status, count) -> count + 1));

        int totalMinutes = entries.stream().mapToInt(TimeEntry::getMinutes).sum();
        long completedTasks = tasksByStatus.get(TaskStatus.DONE);
        long pendingTasks = tasks.size() - completedTasks - tasksByStatus.get(TaskStatus.CANCELLED);

        Map<UUID, UserAccumulator> byUser = new LinkedHashMap<>();
        for (TimeEntry entry : entries) {
            User user = entry.getUser();
            byUser.computeIfAbsent(user.getId(), ignored -> new UserAccumulator(user.getId(), user.getName()))
                    .add(entry.getMinutes());
        }

        BigDecimal hourlyRate = project.getHourlyRate() == null ? BigDecimal.ZERO : project.getHourlyRate();
        List<ProjectReportResponse.UserHours> hoursByUser = byUser.values().stream()
                .map(user -> new ProjectReportResponse.UserHours(
                        user.userId,
                        user.userName,
                        user.minutes,
                        toHours(user.minutes),
                        calculateAmount(user.minutes, hourlyRate)
                ))
                .toList();

        BigDecimal completion = tasks.isEmpty()
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(completedTasks * 100L)
                        .divide(BigDecimal.valueOf(tasks.size()), 2, RoundingMode.HALF_UP);

        return new ProjectReportResponse(
                project.getId(), project.getName(), fromDate, toDate,
                totalMinutes, toHours(totalMinutes), calculateAmount(totalMinutes, hourlyRate),
                tasks.size(), completedTasks, pendingTasks, completion,
                tasksByStatus, hoursByUser
        );
    }

    @Transactional(readOnly = true)
    public byte[] exportProjectSummaryCsv(UUID projectId, LocalDate fromDate, LocalDate toDate) {
        ProjectReportResponse report = getProjectSummary(projectId, fromDate, toDate);
        StringBuilder csv = new StringBuilder("user_id,user_name,minutes,hours,billable_amount\r\n");
        report.hoursByUser().forEach(row -> csv
                .append(row.userId()).append(',')
                .append(escapeCsv(row.userName())).append(',')
                .append(row.minutes()).append(',')
                .append(row.hours()).append(',')
                .append(row.billableAmount()).append("\r\n"));
        csv.append("TOTAL,,").append(report.totalMinutes()).append(',')
                .append(report.totalHours()).append(',').append(report.billableAmount()).append("\r\n");
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Project findAndValidateAccess(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));
        User current = getCurrentUser();
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            throw new SecurityException("No tienes acceso a este proyecto");
        }
        return project;
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private BigDecimal toHours(int minutes) {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAmount(int minutes, BigDecimal hourlyRate) {
        return BigDecimal.valueOf(minutes)
                .multiply(hourlyRate)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static final class UserAccumulator {
        private final UUID userId;
        private final String userName;
        private int minutes;

        private UserAccumulator(UUID userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }

        private void add(int value) {
            minutes += value;
        }
    }
}
