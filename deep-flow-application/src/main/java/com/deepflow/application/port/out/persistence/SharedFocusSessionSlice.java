package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SessionRepository 의 크루 피드 조회 결과. (sharedAt, id) 복합 커서를 보존해
 * 다음 페이지 호출 시 정렬 키와 일치하는 keyset 조건으로 이어갈 수 있게 한다.
 */
public record SharedFocusSessionSlice(
        List<FocusSession> content,
        LocalDateTime nextSharedAt,
        Long nextId,
        boolean hasNext
) {}
