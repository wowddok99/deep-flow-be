package com.deepflow.api.dto;

import com.deepflow.core.domain.session.SessionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Session detail response")
public record SessionDetailResponse(
    @Schema(description = "Session ID", example = "1")
    Long id,
    @Schema(description = "Session start time", example = "2025-01-15T10:30:00")
    LocalDateTime startTime,
    @Schema(description = "Session end time", example = "2025-01-15T12:00:00")
    LocalDateTime endTime,
    @Schema(description = "Duration in seconds", example = "5400")
    Long durationSeconds,
    @Schema(description = "Session status", example = "COMPLETED")
    SessionStatus status,
    @Schema(description = "TipTap editor content (JSON)")
    JsonNode content,
    @Schema(description = "Log title", example = "Spring Boot study")
    String title,
    @Schema(description = "Log summary", example = "Studied JPA and Spring Security")
    String summary,
    @Schema(description = "AI-generated summary")
    String aiSummary,
    @Schema(description = "Attached image URLs")
    List<String> imageUrls
) {
}
