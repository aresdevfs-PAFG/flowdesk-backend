package com.areswayne.flowdesk.domain.workspace.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        String slug,
        String ownerName,
        String ownerEmail,
        int memberCount,
        LocalDateTime createdAt
) {}