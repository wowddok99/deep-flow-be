package com.deepflow.api.dto;

import com.deepflow.core.domain.session.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
@Schema(description = "Session start response")
public record SessionResponse(
    @Schema(description = "Session ID", example = "1")
    Long id,
    @Schema(description = "Session start time", example = "2025-01-15T10:30:00")
    LocalDateTime startTime,
    @Schema(description = "Session status", example = "ONGOING")
    SessionStatus status
) {
}
