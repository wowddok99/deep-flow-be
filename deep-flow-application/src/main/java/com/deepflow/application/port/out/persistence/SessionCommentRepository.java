package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.comment.SessionComment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SessionCommentRepository {

    SessionComment save(SessionComment comment);

    Optional<SessionComment> findById(Long id);

    /**
     * 댓글 트리 보존을 위해 삭제된 부모 댓글도 포함해 평면 구조로 조회
     */
    List<SessionComment> findAllBySessionIdWithUser(Long sessionId);

    /**
     * 알림 이동 경로 조립에서 댓글, 작성자, 세션 ID 조회 N+1 방지
     */
    List<SessionComment> findAllByIdsWithUser(List<Long> ids);

    Map<Long, Integer> countBySessionIds(List<Long> sessionIds);
}
