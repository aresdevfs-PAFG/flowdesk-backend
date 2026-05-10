package com.areswayne.flowdesk.domain.timeentry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByTaskIdOrderByStartedAtDesc(UUID taskId);

    List<TimeEntry> findByUserIdOrderByStartedAtDesc(UUID userId);

    Optional<TimeEntry> findByUserIdAndEndedAtIsNull(UUID userId);

    @Query("SELECT COALESCE(SUM(te.minutes), 0) FROM TimeEntry te WHERE te.task.project.id = :projectId")
    int sumMinutesByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COALESCE(SUM(te.minutes), 0) FROM TimeEntry te WHERE te.task.id = :taskId")
    int sumMinutesByTaskId(@Param("taskId") UUID taskId);

    // Nuevo — para facturación con filtro de fechas opcional
    @Query("""
        SELECT te FROM TimeEntry te
        WHERE te.task.project.id = :projectId
          AND te.endedAt IS NOT NULL
          AND (:fromDate IS NULL OR CAST(te.startedAt AS date) >= :fromDate)
          AND (:toDate IS NULL OR CAST(te.startedAt AS date) <= :toDate)
        ORDER BY te.startedAt ASC
    """)
    List<TimeEntry> findByProjectId(
            @Param("projectId") UUID projectId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}