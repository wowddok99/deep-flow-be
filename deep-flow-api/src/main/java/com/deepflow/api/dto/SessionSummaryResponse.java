package com.deepflow.api.dto;

import com.deepflow.application.session.dto.SessionSummaryInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
@Schema(description = "Session summary for list view")
public record SessionSummaryResponse(
    @Schema(description = "Session ID", example = "1")
    Long id,
    @Schema(description = "Session start time", example = "2025-01-15T10:30:00")
    LocalDateTime startTime,
    @Schema(description = "Session end time", example = "2025-01-15T12:00:00")
    LocalDateTime endTime,
    @Schema(description = "Duration in seconds", example = "5400")
    Long durationSeconds,
    @Schema(description = "Session status", example = "COMPLETED")
    String status,
    @Schema(description = "Log title", example = "Spring Boot study")
    String title,
    @Schema(description = "Log summary", example = "Studied JPA and Spring Security")
    String summary
) {
    public static SessionSummaryResponse from(SessionSummaryInfo info) {
        return new SessionSummaryResponse(
            info.id(), info.startTime(), info.endTime(),
            info.durationSeconds(), info.status(),
            info.title(), info.summary()
        );
    }
}
