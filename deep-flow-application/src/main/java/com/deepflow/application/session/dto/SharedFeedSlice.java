package com.deepflow.application.session.dto;

import java.util.List;

/**
 * 크루 피드 응답용 슬라이스. 외부 표면 노출은 불투명 토큰 형태의 nextCursor 만.
 */
public record SharedFeedSlice<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {}
