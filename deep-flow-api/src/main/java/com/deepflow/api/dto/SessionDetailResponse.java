package com.deepflow.api.dto;

import com.deepflow.application.session.dto.SessionDetailInfo;
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
    String status,
    @Schema(description = "TipTap editor content (JSON)")
    JsonNode content,
    @Schema(description = "Log title", example = "Spring Boot study")
    String title,
    @Schema(description = "Log summary", example = "Studied JPA and Spring Security")
    String summary,
    @Schema(description = "Attached image URLs")
    List<String> imageUrls,
    @Schema(description = "Crew this session is shared to (null if not shared)", example = "5")
    Long sharedCrewId,
    @Schema(description = "When this session was shared (null if not shared)")
    LocalDateTime sharedAt
) {
    public static SessionDetailResponse from(SessionDetailInfo info, JsonNode contentNode) {
        return new SessionDetailResponse(
            info.id(), info.startTime(), info.endTime(),
            info.durationSeconds(), info.status(),
            contentNode, info.title(), info.summary(),
            info.imageUrls(),
            info.sharedCrewId(), info.sharedAt()
        );
    }
}
