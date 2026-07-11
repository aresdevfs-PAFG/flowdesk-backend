package com.areswayne.flowdesk.domain.report;

import com.areswayne.flowdesk.domain.project.Project;
import com.areswayne.flowdesk.domain.project.ProjectMemberRepository;
import com.areswayne.flowdesk.domain.project.ProjectRepository;
import com.areswayne.flowdesk.domain.task.Task;
import com.areswayne.flowdesk.domain.task.TaskRepository;
import com.areswayne.flowdesk.domain.timeentry.TimeEntry;
import com.areswayne.flowdesk.domain.timeentry.TimeEntryRepository;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.shared.enums.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TimeEntryRepository timeEntryRepository;
    @Mock private UserRepository userRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(projectRepository, projectMemberRepository,
                taskRepository, timeEntryRepository, userRepository);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager@flowdesk.test", null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void calculatesProjectSummaryByUserAndStatus() {
        UUID projectId = UUID.randomUUID();
        User manager = user(UUID.randomUUID(), "Manager", "manager@flowdesk.test");
        User collaborator = user(UUID.randomUUID(), "Colaborador", "collab@flowdesk.test");
        Project project = Project.builder().id(projectId).name("Portal")
                .hourlyRate(new BigDecimal("600.00")).build();

        Task done = Task.builder().id(UUID.randomUUID()).project(project).status(TaskStatus.DONE).build();
        Task pending = Task.builder().id(UUID.randomUUID()).project(project).status(TaskStatus.IN_PROGRESS).build();
        TimeEntry first = TimeEntry.builder().user(manager).task(done).minutes(90).build();
        TimeEntry second = TimeEntry.builder().user(collaborator).task(pending).minutes(30).build();

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, manager.getId())).thenReturn(true);
        when(taskRepository.findByProjectIdOrderByPosition(projectId)).thenReturn(List.of(done, pending));
        when(timeEntryRepository.findByProjectId(projectId, null, null)).thenReturn(List.of(first, second));

        var report = reportService.getProjectSummary(projectId, null, null);

        assertThat(report.totalMinutes()).isEqualTo(120);
        assertThat(report.totalHours()).isEqualByComparingTo("2.00");
        assertThat(report.billableAmount()).isEqualByComparingTo("1200.00");
        assertThat(report.completedTasks()).isEqualTo(1);
        assertThat(report.pendingTasks()).isEqualTo(1);
        assertThat(report.completionPercentage()).isEqualByComparingTo("50.00");
        assertThat(report.hoursByUser()).hasSize(2);
    }

    private User user(UUID id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }
}
