package com.deepflow.api.dto.crew;

import com.deepflow.application.session.dto.MemberSuggestionInfo;

public record MemberSuggestResponse(
        Long userId,
        String name,
        String username
) {
    public static MemberSuggestResponse from(MemberSuggestionInfo info) {
        return new MemberSuggestResponse(info.userId(), info.name(), info.username());
    }
}
