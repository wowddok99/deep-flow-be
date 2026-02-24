package com.deepflow.application.session.dto;

import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;

public record SessionSummaryInfo(
    Long id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationSeconds,
    String status,
    String title,
    String summary
) {
    public static SessionSummaryInfo from(FocusSession session) {
        return new SessionSummaryInfo(
            session.getId(),
            session.getStartTime(),
            session.getEndTime(),
            session.getDurationSeconds(),
            session.getStatus().name(),
            session.getFocusLog().getTitle(),
            session.getFocusLog().getSummary()
        );
    }
}
