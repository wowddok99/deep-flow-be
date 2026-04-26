package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.CrewHighlightInfo;
import com.deepflow.application.session.dto.HighlightItemInfo;
import com.deepflow.application.session.dto.HighlightMode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record CrewHighlightResponse(
        HighlightMode mode,
        List<Item> items,
        int recentSharedCount,
        int threshold
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(
            HighlightItemInfo.Type type,
            Long sessionId,
            String title,
            String userName,
            Double score,
            Long durationSeconds,
            String tag,
            Integer count
    ) {
        public static Item from(HighlightItemInfo i) {
            return new Item(i.type(), i.sessionId(), i.title(), i.userName(),
                    i.score(), i.durationSeconds(), i.tag(), i.count());
        }
    }

    public static CrewHighlightResponse from(CrewHighlightInfo info) {
        return new CrewHighlightResponse(
                info.mode(),
                info.items().stream().map(Item::from).toList(),
                info.recentSharedCount(),
                info.threshold()
        );
    }
}
