package com.deepflow.application.session.share.dto;

import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;
import java.util.List;

public record SharedSessionInfo(
        Long sessionId,
        Long crewId,
        LocalDateTime sharedAt,
        List<String> tags
) {
    public static SharedSessionInfo from(FocusSession session, List<String> tags) {
        return new SharedSessionInfo(
                session.getId(),
                session.getSharedCrewId(),
                session.getSharedAt(),
                tags
        );
    }
}
