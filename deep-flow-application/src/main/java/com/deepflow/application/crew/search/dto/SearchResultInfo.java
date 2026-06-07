package com.deepflow.application.crew.search.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SearchResultInfo(
        Long sessionId,
        String title,
        String summaryPreview,
        String userName,
        Long userId,
        List<String> tags,
        LocalDateTime sharedAt,
        double score
) {}
