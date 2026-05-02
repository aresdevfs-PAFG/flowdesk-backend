package com.areswayne.flowdesk.domain.project;

import com.areswayne.flowdesk.domain.project.dto.AddProjectMemberRequest;
import com.areswayne.flowdesk.domain.project.dto.ProjectRequest;
import com.areswayne.flowdesk.domain.project.dto.ProjectResponse;
import com.areswayne.flowdesk.shared.enums.ProjectStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(workspaceId, request));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getByWorkspace(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(projectService.getByWorkspace(workspaceId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID workspaceId,
                                                    @PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getById(projectId));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.update(projectId, request));
    }

    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ProjectResponse> updateStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @RequestParam ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateStatus(projectId, status));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(@PathVariable UUID workspaceId,
                                        @PathVariable UUID projectId) {
        projectService.delete(projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request) {
        projectService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}