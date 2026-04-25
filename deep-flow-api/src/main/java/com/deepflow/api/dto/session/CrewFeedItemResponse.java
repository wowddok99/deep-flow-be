package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.CrewFeedItemInfo;

import java.time.LocalDateTime;
import java.util.List;

public record CrewFeedItemResponse(
        Long sessionId,
        UserBrief user,
        String title,
        String summaryPreview,
        Long durationSeconds,
        LocalDateTime sharedAt,
        List<String> tags,
        int reactionCount,
        int commentCount
) {
    public static CrewFeedItemResponse from(CrewFeedItemInfo info) {
        return new CrewFeedItemResponse(
                info.sessionId(),
                new UserBrief(info.userId(), info.userName()),
                info.title(),
                info.summaryPreview(),
                info.durationSeconds(),
                info.sharedAt(),
                info.tags(),
                info.reactionCount(),
                info.commentCount()
        );
    }

    public record UserBrief(Long id, String name) {}
}
