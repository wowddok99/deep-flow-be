package com.deepflow.api.dto.session;

import com.deepflow.application.notification.dto.MentionInfo;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long commentId,
        Long sessionId,
        Long crewId,
        String actorName,
        String contentPreview,
        LocalDateTime createdAt,
        boolean read
) {
    public static NotificationResponse from(MentionInfo m) {
        return new NotificationResponse(
                m.id(),
                m.commentId(),
                m.sessionId(),
                m.crewId(),
                m.actorName(),
                m.contentPreview(),
                m.createdAt(),
                m.read()
        );
    }
}
