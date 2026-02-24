package com.deepflow.application.session.dto;

import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;

public record SessionInfo(
    Long id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationSeconds,
    String status
) {
    public static SessionInfo from(FocusSession session) {
        return new SessionInfo(
            session.getId(),
            session.getStartTime(),
            session.getEndTime(),
            session.getDurationSeconds(),
            session.getStatus().name()
        );
    }
}
