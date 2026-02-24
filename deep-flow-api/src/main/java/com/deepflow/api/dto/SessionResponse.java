package com.deepflow.api.dto;

import com.deepflow.application.session.dto.SessionInfo;
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
    String status
) {
    public static SessionResponse from(SessionInfo info) {
        return new SessionResponse(info.id(), info.startTime(), info.status());
    }
}
