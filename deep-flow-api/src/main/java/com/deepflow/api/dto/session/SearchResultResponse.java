package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.SearchResultInfo;

import java.time.LocalDateTime;
import java.util.List;

public record SearchResultResponse(
        Long sessionId,
        String title,
        String summaryPreview,
        Author user,
        List<String> tags,
        LocalDateTime sharedAt,
        double score
) {
    public record Author(Long id, String name) {}

    public static SearchResultResponse from(SearchResultInfo info) {
        return new SearchResultResponse(
                info.sessionId(),
                info.title(),
                info.summaryPreview(),
                new Author(info.userId(), info.userName()),
                info.tags(),
                info.sharedAt(),
                info.score()
        );
    }
}
