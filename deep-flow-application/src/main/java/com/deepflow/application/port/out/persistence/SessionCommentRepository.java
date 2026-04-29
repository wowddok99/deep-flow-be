package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.comment.SessionComment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SessionCommentRepository {

    SessionComment save(SessionComment comment);

    Optional<SessionComment> findById(Long id);

    /**
     * 세션의 모든 댓글을 flat 으로 조회 (User fetch join). soft-deleted 도 포함 — 자식 보존을 위해.
     */
    List<SessionComment> findAllBySessionIdWithUser(Long sessionId);

    /**
     * 댓글 ID 목록으로 batch 조회 (User fetch join).
     * 알림 deep link 매핑용 — mention.commentId → comment.user/content/sessionId 풀어내기.
     */
    List<SessionComment> findAllByIdsWithUser(List<Long> ids);

    /**
     * 피드 batch 카운트. soft-deleted 제외.
     */
    Map<Long, Integer> countBySessionIds(List<Long> sessionIds);
}
