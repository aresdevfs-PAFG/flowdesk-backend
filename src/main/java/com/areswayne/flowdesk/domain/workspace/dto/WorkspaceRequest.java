package com.areswayne.flowdesk.domain.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String name,

        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 100)
        @Pattern(
            regexp = "^[a-z0-9-]+$",
            message = "El slug solo puede contener letras minúsculas, números y guiones"
        )
        String slug
) {}