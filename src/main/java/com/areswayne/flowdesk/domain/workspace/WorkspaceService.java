package com.areswayne.flowdesk.domain.workspace;

import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.domain.workspace.dto.AddMemberRequest;
import com.areswayne.flowdesk.domain.workspace.dto.WorkspaceRequest;
import com.areswayne.flowdesk.domain.workspace.dto.WorkspaceResponse;
import com.areswayne.flowdesk.shared.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse create(WorkspaceRequest request) {
        User owner = getCurrentUser();

        if (workspaceRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("El slug ya está en uso");
        }

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .slug(request.slug())
                .owner(owner)
                .build();

        workspaceRepository.save(workspace);

        // El owner se agrega automáticamente como miembro ADMIN
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(owner)
                .role(Role.ADMIN)
                .build();

        memberRepository.save(ownerMember);

        return toResponse(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces() {
        User current = getCurrentUser();
        return workspaceRepository.findAllByMemberId(current.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getById(UUID id) {
        Workspace workspace = findAndValidateAccess(id);
        return toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse update(UUID id, WorkspaceRequest request) {
        Workspace workspace = findAndValidateOwnership(id);

        if (!workspace.getSlug().equals(request.slug())
                && workspaceRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("El slug ya está en uso");
        }

        workspace.setName(request.name());
        workspace.setSlug(request.slug());

        return toResponse(workspaceRepository.save(workspace));
    }

    @Transactional
    public void delete(UUID id) {
        findAndValidateOwnership(id);
        workspaceRepository.deleteById(id);
    }

    @Transactional
    public void addMember(UUID workspaceId, AddMemberRequest request) {
        Workspace workspace = findAndValidateOwnership(workspaceId);

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.userId())) {
            throw new IllegalArgumentException("El usuario ya es miembro de este workspace");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(request.role())
                .build();

        memberRepository.save(member);
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID userId) {
        findAndValidateOwnership(workspaceId);

        WorkspaceMember member = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no es miembro"));

        memberRepository.delete(member);
    }

    // ── Helpers ──────────────────────────────────────────────

    private Workspace findAndValidateAccess(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace no encontrado"));

        User current = getCurrentUser();
        boolean isMember = memberRepository
                .existsByWorkspaceIdAndUserId(workspaceId, current.getId());

        if (!isMember) {
            throw new SecurityException("No tienes acceso a este workspace");
        }
        return workspace;
    }

    private Workspace findAndValidateOwnership(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace no encontrado"));

        User current = getCurrentUser();
        if (!workspace.getOwner().getId().equals(current.getId())) {
            throw new SecurityException("Solo el owner puede realizar esta acción");
        }
        return workspace;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private WorkspaceResponse toResponse(Workspace w) {
        return new WorkspaceResponse(
                w.getId(),
                w.getName(),
                w.getSlug(),
                w.getOwner().getName(),
                w.getOwner().getEmail(),
                memberRepository.countByWorkspaceId(w.getId()),
                w.getCreatedAt()
        );
    }
}