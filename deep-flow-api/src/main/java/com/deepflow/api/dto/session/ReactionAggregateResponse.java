package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.ReactionAggregateInfo;

import java.util.List;

public record ReactionAggregateResponse(
        List<EmojiCount> items
) {
    public record EmojiCount(
            String emoji,
            int count,
            boolean userReacted,
            List<Reactor> topReactors
    ) {}

    public record Reactor(Long userId, String name) {}

    public static ReactionAggregateResponse from(ReactionAggregateInfo info) {
        List<EmojiCount> items = info.items().stream()
                .map(i -> new EmojiCount(
                        i.emoji(),
                        i.count(),
                        i.userReacted(),
                        i.topReactors().stream()
                                .map(r -> new Reactor(r.userId(), r.name()))
                                .toList()
                ))
                .toList();
        return new ReactionAggregateResponse(items);
    }
}
