package com.areswayne.flowdesk.domain.project;

import com.areswayne.flowdesk.domain.project.dto.AddProjectMemberRequest;
import com.areswayne.flowdesk.domain.project.dto.ProjectRequest;
import com.areswayne.flowdesk.domain.project.dto.ProjectResponse;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.domain.workspace.WorkspaceMemberRepository;
import com.areswayne.flowdesk.domain.workspace.WorkspaceRepository;
import com.areswayne.flowdesk.shared.enums.ProjectRole;
import com.areswayne.flowdesk.shared.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse create(UUID workspaceId, ProjectRequest request) {
        User current = getCurrentUser();
        validateWorkspaceAccess(workspaceId, current.getId());

        if (projectRepository.existsByWorkspaceIdAndName(workspaceId, request.name())) {
            throw new IllegalArgumentException("Ya existe un proyecto con ese nombre en el workspace");
        }

        var workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace no encontrado"));

        Project project = Project.builder()
                .workspace(workspace)
                .name(request.name())
                .description(request.description())
                .status(ProjectStatus.ACTIVE)
                .hourlyRate(request.hourlyRate())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        projectRepository.save(project);

        // El creador se agrega automáticamente como MANAGER
        ProjectMember manager = ProjectMember.builder()
                .project(project)
                .user(current)
                .role(ProjectRole.MANAGER)
                .build();

        projectMemberRepository.save(manager);

        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getByWorkspace(UUID workspaceId) {
        User current = getCurrentUser();
        validateWorkspaceAccess(workspaceId, current.getId());
        return projectRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID projectId) {
        Project project = findAndValidateAccess(projectId);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse update(UUID projectId, ProjectRequest request) {
        Project project = findAndValidateManager(projectId);

        if (!project.getName().equals(request.name()) &&
                projectRepository.existsByWorkspaceIdAndName(
                        project.getWorkspace().getId(), request.name())) {
            throw new IllegalArgumentException("Ya existe un proyecto con ese nombre en el workspace");
        }

        project.setName(request.name());
        project.setDescription(request.description());
        project.setHourlyRate(request.hourlyRate());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());

        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateStatus(UUID projectId, ProjectStatus status) {
        Project project = findAndValidateManager(projectId);
        project.setStatus(status);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(UUID projectId) {
        findAndValidateManager(projectId);
        projectRepository.deleteById(projectId);
    }

    @Transactional
    public void addMember(UUID projectId, AddProjectMemberRequest request) {
        findAndValidateManager(projectId);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new IllegalArgumentException("El usuario ya es miembro del proyecto");
        }

        var project = projectRepository.findById(projectId).orElseThrow();
        var user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(request.role())
                .build();

        projectMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        findAndValidateManager(projectId);

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no es miembro del proyecto"));

        projectMemberRepository.delete(member);
    }

    // ── Helpers ──────────────────────────────────────────────

    private void validateWorkspaceAccess(UUID workspaceId, UUID userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new SecurityException("No tienes acceso a este workspace");
        }
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

    private Project findAndValidateManager(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        User current = getCurrentUser();
        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, current.getId())
                .orElseThrow(() -> new SecurityException("No tienes acceso a este proyecto"));

        if (member.getRole() != ProjectRole.MANAGER) {
            throw new SecurityException("Solo un MANAGER puede realizar esta acción");
        }
        return project;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getWorkspace().getId(),
                p.getName(),
                p.getDescription(),
                p.getStatus(),
                p.getHourlyRate(),
                p.getStartDate(),
                p.getEndDate(),
                projectMemberRepository.countByProjectId(p.getId()),
                projectRepository.countTasksByProjectId(p.getId()),
                p.getCreatedAt()
        );
    }
}