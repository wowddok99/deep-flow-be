package com.deepflow.api.dto;

import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record SessionSummaryResponse(
    Long id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationSeconds,
    SessionStatus status,
    String summary
) {
    public static SessionSummaryResponse from(FocusSession session) {
        return SessionSummaryResponse.builder()
            .id(session.getId())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .durationSeconds(session.getDurationSeconds())
            .status(session.getStatus())
            .summary(session.getFocusLog().getSummary())
            .build();
    }
}
