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
        int commentCount
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
                commentCount
        );
    }

    public static CrewFeedItemInfo from(FocusSession session, List<String> tags) {
        return from(session, tags, 0, 0);
    }

    private static String buildPreview(FocusLog log) {
        // FE (SessionEditorSheet/SessionDetailSheet) 가 저장 시 content 에서 평문 추출해 summary 로 보냄.
        // BE 는 summary 만 신뢰. 비어있으면 null — content fallback 시 raw JSON 이 노출되는 문제 회피.
        if (log == null || log.getSummary() == null || log.getSummary().isBlank()) return null;
        String s = log.getSummary().trim();
        return s.length() <= PREVIEW_MAX_LENGTH ? s : s.substring(0, PREVIEW_MAX_LENGTH);
    }
}
