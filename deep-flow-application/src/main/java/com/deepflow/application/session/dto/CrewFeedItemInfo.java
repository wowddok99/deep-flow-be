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

    public static CrewFeedItemInfo from(FocusSession session, List<String> tags) {
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
                0,
                0
        );
    }

    private static String buildPreview(FocusLog log) {
        if (log == null) return null;
        // summary 우선, 없으면 content 첫 N 자
        if (log.getSummary() != null && !log.getSummary().isBlank()) {
            String s = log.getSummary().trim();
            return s.length() <= PREVIEW_MAX_LENGTH ? s : s.substring(0, PREVIEW_MAX_LENGTH);
        }
        String content = log.getContent();
        if (content == null || content.isBlank() || "{}".equals(content.trim())) return null;
        // content 가 JSON 인 경우(에디터 포맷) 그대로 자르면 깨질 수 있어 best-effort 처리.
        String s = content.trim();
        return s.length() <= PREVIEW_MAX_LENGTH ? s : s.substring(0, PREVIEW_MAX_LENGTH);
    }
}
