package com.areswayne.flowdesk.domain.task.dto;

import jakarta.validation.constraints.NotNull;

public record ReorderRequest(
        @NotNull
        Integer newPosition
) {}