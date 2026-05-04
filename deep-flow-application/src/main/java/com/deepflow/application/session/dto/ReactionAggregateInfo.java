package com.deepflow.application.session.dto;

import java.util.List;

public record ReactionAggregateInfo(
        List<EmojiCountInfo> items
) {
    public record EmojiCountInfo(
            String emoji,
            int count,
            boolean userReacted,
            List<ReactorSummary> topReactors
    ) {}

    public record ReactorSummary(
            Long userId,
            String name
    ) {}
}
