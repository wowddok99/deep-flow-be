package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.MentionInfo;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long commentId,
        LocalDateTime createdAt,
        boolean read
) {
    public static NotificationResponse from(MentionInfo m) {
        return new NotificationResponse(m.id(), m.commentId(), m.createdAt(), m.read());
    }
}
