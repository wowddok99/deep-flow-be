package com.deepflow.application.session.dto;

import com.deepflow.domain.session.comment.CommentMention;

import java.time.LocalDateTime;

public record MentionInfo(
        Long id,
        Long commentId,
        LocalDateTime createdAt,
        boolean read
) {
    public static MentionInfo from(CommentMention m) {
        return new MentionInfo(m.getId(), m.getCommentId(), m.getCreatedAt(), m.isRead());
    }
}
