package com.deepflow.api.dto;

import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record SessionResponse(
    Long id,
    LocalDateTime startTime,
    SessionStatus status
) {
    public static SessionResponse from(FocusSession session) {
        return SessionResponse.builder()
            .id(session.getId())
            .startTime(session.getStartTime())
            .status(session.getStatus())
            .build();
    }
}
