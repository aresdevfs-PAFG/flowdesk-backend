package com.areswayne.flowdesk.domain.project;

import com.areswayne.flowdesk.shared.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByWorkspaceId(UUID workspaceId);

    List<Project> findByWorkspaceIdAndStatus(UUID workspaceId, ProjectStatus status);

    boolean existsByWorkspaceIdAndName(UUID workspaceId, String name);

    @Query("""
        SELECT p FROM Project p
        JOIN ProjectMember pm ON pm.project.id = p.id
        WHERE pm.user.id = :userId
    """)
    List<Project> findAllByMemberId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countTasksByProjectId(@Param("projectId") UUID projectId);
}