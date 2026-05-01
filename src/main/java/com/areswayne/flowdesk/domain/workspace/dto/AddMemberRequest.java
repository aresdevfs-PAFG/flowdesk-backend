package com.areswayne.flowdesk.domain.workspace.dto;

import com.areswayne.flowdesk.shared.enums.Role;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(

        @NotNull(message = "El userId es obligatorio")
        UUID userId,

        @NotNull(message = "El rol es obligatorio")
        Role role
) {}