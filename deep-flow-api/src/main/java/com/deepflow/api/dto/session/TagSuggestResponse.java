package com.deepflow.api.dto.session;

import com.deepflow.application.session.tag.dto.TagSuggestInfo;

public record TagSuggestResponse(String tag, long count) {
    public static TagSuggestResponse from(TagSuggestInfo info) {
        return new TagSuggestResponse(info.tag(), info.count());
    }
}
