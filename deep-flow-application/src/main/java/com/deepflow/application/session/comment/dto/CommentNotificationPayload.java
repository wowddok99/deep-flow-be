package com.deepflow.application.session.comment.dto;

public record CommentNotificationPayload(
        Type type,
        Long sessionId,
        Long commentId,
        Long actorUserId,
        String actorName,
        String contentPreview
) {
    public enum Type { COMMENT_ON_YOUR_POST, MENTION }
}
