package com.areswayne.flowdesk.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(

        @NotBlank(message = "El comentario no puede estar vacío")
        @Size(max = 2000, message = "Máximo 2000 caracteres")
        String body
) {}