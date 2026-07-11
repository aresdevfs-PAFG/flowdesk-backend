package com.areswayne.flowdesk.domain.task;

import com.areswayne.flowdesk.shared.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectIdOrderByPosition(UUID projectId);

    List<Task> findByProjectIdAndStatusOrderByPosition(UUID projectId, TaskStatus status);

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByDueDateBetweenAndAssigneeIsNotNullAndStatusNotIn(
            LocalDate fromDate, LocalDate toDate, List<TaskStatus> excludedStatuses);

    @Query("SELECT COALESCE(MAX(t.position), 0) FROM Task t WHERE t.project.id = :projectId")
    int findMaxPositionByProjectId(@Param("projectId") UUID projectId);
}
