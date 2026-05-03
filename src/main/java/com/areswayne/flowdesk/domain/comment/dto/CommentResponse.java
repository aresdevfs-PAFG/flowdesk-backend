package com.areswayne.flowdesk.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID taskId,
        UUID authorId,
        String authorName,
        String authorAvatarUrl,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}