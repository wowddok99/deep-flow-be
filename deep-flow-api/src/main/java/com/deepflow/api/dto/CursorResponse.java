package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Cursor-based pagination response")
public record CursorResponse<T>(
    @Schema(description = "Content list")
    List<T> content,
    @Schema(description = "Next cursor ID for pagination", example = "5")
    Long nextCursorId,
    @Schema(description = "Whether more data exists")
    boolean hasNext
) {}
