package com.deepflow.application.crew.dto;

import java.util.List;

/**
 * 크루 피드 응답에서 내부 정렬 키를 숨기기 위한 불투명 커서 슬라이스
 */
public record SharedFeedSlice<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {}
