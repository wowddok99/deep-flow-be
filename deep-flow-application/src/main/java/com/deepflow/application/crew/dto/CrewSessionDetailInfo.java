package com.deepflow.application.crew.dto;

import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;
import java.util.List;

public record CrewSessionDetailInfo(
        Long sessionId,
        Long userId,
        String userName,
        String title,
        String content,
        Long durationSeconds,
        LocalDateTime sharedAt,
        List<String> tags,
        int reactionCount,
        int commentCount,
        boolean edited
) {
    public static CrewSessionDetailInfo from(FocusSession session, List<String> tags, int reactionCount, int commentCount) {
        FocusLog log = session.getFocusLog();
        String title = log != null ? log.getTitle() : null;
        String content = log != null ? log.getContent() : null;

        return new CrewSessionDetailInfo(
                session.getId(),
                session.getUser() != null ? session.getUser().getId() : null,
                session.getUser() != null ? session.getUser().getName() : "알수없음",
                title,
                content,
                session.getDurationSeconds(),
                session.getSharedAt(),
                tags == null ? List.of() : tags,
                reactionCount,
                commentCount,
                computeEdited(session)
        );
    }

    private static boolean computeEdited(FocusSession session) {
        FocusLog log = session.getFocusLog();
        if (log == null || log.getUpdatedAt() == null) return false;
        if (session.getSharedAt() == null) return false;
        return log.getUpdatedAt().isAfter(session.getSharedAt());
    }
}
