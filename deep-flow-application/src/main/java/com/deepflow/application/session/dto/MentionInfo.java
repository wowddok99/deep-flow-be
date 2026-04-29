package com.deepflow.application.session.dto;

import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.comment.CommentMention;
import com.deepflow.domain.session.comment.SessionComment;

import java.time.LocalDateTime;

public record MentionInfo(
        Long id,
        Long commentId,
        Long sessionId,
        Long crewId,
        String actorName,
        String contentPreview,
        LocalDateTime createdAt,
        boolean read
) {
    private static final int PREVIEW_MAX_LENGTH = 100;

    /**
     * Deep-link 메타까지 채운 풀 매핑.
     * comment / session 이 null 이면 (정합성 어긋난 비정상 상태) 가능한 부분만 채우고 나머지는 null.
     */
    public static MentionInfo of(CommentMention mention, SessionComment comment, FocusSession session) {
        Long sessionId = comment != null ? comment.getSessionId() : null;
        Long crewId = session != null ? session.getSharedCrewId() : null;
        String actorName = (comment != null && comment.getUser() != null) ? comment.getUser().getName() : null;
        String preview = buildPreview(comment);

        return new MentionInfo(
                mention.getId(),
                mention.getCommentId(),
                sessionId,
                crewId,
                actorName,
                preview,
                mention.getCreatedAt(),
                mention.isRead()
        );
    }

    private static String buildPreview(SessionComment comment) {
        if (comment == null || comment.getContent() == null) return null;
        if (comment.isDeleted()) return null;
        String content = comment.getContent();
        return content.length() <= PREVIEW_MAX_LENGTH
                ? content
                : content.substring(0, PREVIEW_MAX_LENGTH);
    }
}
