package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 크루 피드 조회 결과와 다음 페이지 정렬 키 보관
 */
public record SharedFocusSessionSlice(
        List<FocusSession> content,
        LocalDateTime nextSharedAt,
        Long nextId,
        boolean hasNext
) {}
