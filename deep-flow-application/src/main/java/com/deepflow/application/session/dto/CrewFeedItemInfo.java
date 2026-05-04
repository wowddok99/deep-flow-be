package com.deepflow.application.session.dto;

import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;
import java.util.List;

public record CrewFeedItemInfo(
        Long sessionId,
        Long userId,
        String userName,
        String title,
        String summaryPreview,
        Long durationSeconds,
        LocalDateTime sharedAt,
        List<String> tags,
        int reactionCount,
        int commentCount,
        boolean edited
) {
    private static final int PREVIEW_MAX_LENGTH = 100;

    public static CrewFeedItemInfo from(FocusSession session, List<String> tags, int reactionCount, int commentCount) {
        FocusLog log = session.getFocusLog();
        String title = log != null ? log.getTitle() : null;
        String preview = buildPreview(log);

        return new CrewFeedItemInfo(
                session.getId(),
                session.getUser() != null ? session.getUser().getId() : null,
                session.getUser() != null ? session.getUser().getName() : "알수없음",
                title,
                preview,
                session.getDurationSeconds(),
                session.getSharedAt(),
                tags == null ? List.of() : tags,
                reactionCount,
                commentCount,
                computeEdited(session)
        );
    }

    public static CrewFeedItemInfo from(FocusSession session, List<String> tags) {
        return from(session, tags, 0, 0);
    }

    /**
     * "(편집됨)" 표시
     * - 공유 이후 수정만 true
     * - log.updatedAt 기준
     * - session.updatedAt 사용 금지 (본문 수정 없어도 true 될 수 있음)
     */
    private static boolean computeEdited(FocusSession session) {
        FocusLog log = session.getFocusLog();
        if (log == null || log.getUpdatedAt() == null) return false;
        if (session.getSharedAt() == null) return false;
        return log.getUpdatedAt().isAfter(session.getSharedAt());
    }

    /**
     * summary preview
     * - summary 없으면 null (content fallback 금지)
     * - 최대 100자
     */
    private static String buildPreview(FocusLog log) {
        if (log == null || log.getSummary() == null || log.getSummary().isBlank()) return null;
        String s = log.getSummary().trim();
        return s.length() <= PREVIEW_MAX_LENGTH ? s : s.substring(0, PREVIEW_MAX_LENGTH);
    }
}
