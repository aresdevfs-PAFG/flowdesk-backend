package com.areswayne.flowdesk.domain.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByOwnerId(UUID ownerId);

    boolean existsBySlug(String slug);

    Optional<Workspace> findBySlug(String slug);

    @Query("""
        SELECT w FROM Workspace w
        JOIN WorkspaceMember wm ON wm.workspace.id = w.id
        WHERE wm.user.id = :userId
    """)
    List<Workspace> findAllByMemberId(@Param("userId") UUID userId);
}