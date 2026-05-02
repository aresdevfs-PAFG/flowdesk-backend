package com.areswayne.flowdesk.domain.project.dto;

import com.areswayne.flowdesk.shared.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(

        @NotNull(message = "El userId es obligatorio")
        UUID userId,

        @NotNull(message = "El rol es obligatorio")
        ProjectRole role
) {}