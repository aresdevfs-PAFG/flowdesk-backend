package com.areswayne.flowdesk.domain.timeentry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByTaskIdOrderByStartedAtDesc(UUID taskId);

    List<TimeEntry> findByUserIdOrderByStartedAtDesc(UUID userId);

    // Timer activo — entrada sin ended_at
    Optional<TimeEntry> findByUserIdAndEndedAtIsNull(UUID userId);

    // Total de minutos registrados en un proyecto
    @Query("""
        SELECT COALESCE(SUM(te.minutes), 0)
        FROM TimeEntry te
        WHERE te.task.project.id = :projectId
    """)
    int sumMinutesByProjectId(@Param("projectId") UUID projectId);

    // Total de minutos por tarea
    @Query("SELECT COALESCE(SUM(te.minutes), 0) FROM TimeEntry te WHERE te.task.id = :taskId")
    int sumMinutesByTaskId(@Param("taskId") UUID taskId);
}