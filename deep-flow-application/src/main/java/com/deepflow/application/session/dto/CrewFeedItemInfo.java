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
     * "공유 후 본문 수정된 경우" 만 신호.
     * - 공유 전 수정: 독자가 본 적 없음 → 표시 X
     * - 공유 후 수정: 독자가 본 본문이 바뀜 → 표시 O
     * Slack/Discord 메시지의 "(편집됨)" 정의와 동일.
     */
    private static boolean computeEdited(FocusSession session) {
        return session.getUpdatedAt() != null
                && session.getSharedAt() != null
                && session.getUpdatedAt().isAfter(session.getSharedAt());
    }

    private static String buildPreview(FocusLog log) {
        // FE (SessionEditorSheet/SessionDetailSheet) 가 저장 시 content 에서 평문 추출해 summary 로 보냄.
        // BE 는 summary 만 신뢰. 비어있으면 null — content fallback 시 raw JSON 이 노출되는 문제 회피.
        if (log == null || log.getSummary() == null || log.getSummary().isBlank()) return null;
        String s = log.getSummary().trim();
        return s.length() <= PREVIEW_MAX_LENGTH ? s : s.substring(0, PREVIEW_MAX_LENGTH);
    }
}
