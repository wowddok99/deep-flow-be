package com.deepflow.infra.persistence.session;

import com.deepflow.domain.session.comment.SessionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface SessionCommentJpaRepository extends JpaRepository<SessionComment, Long> {

    @Query("""
            SELECT c FROM SessionComment c
            JOIN FETCH c.user
            WHERE c.sessionId = :sessionId
            ORDER BY c.createdAt ASC, c.id ASC
            """)
    List<SessionComment> findAllBySessionIdWithUser(@Param("sessionId") Long sessionId);

    /**
     * 삭제된 댓글의 멘션은 알림으로 보여도 이동할 본문이 없으므로 unread 목록에서 제외
     *
     * 멘션 데이터 정리는 후속 과제에서 별도 처리
     */
    @Query("""
            SELECT c FROM SessionComment c
            JOIN FETCH c.user
            WHERE c.id IN :ids
              AND c.deletedAt IS NULL
            """)
    List<SessionComment> findAllByIdsWithUser(@Param("ids") List<Long> ids);

    @Query("""
            SELECT c.sessionId AS sessionId, COUNT(c) AS cnt
            FROM SessionComment c
            WHERE c.sessionId IN :sessionIds
              AND c.deletedAt IS NULL
            GROUP BY c.sessionId
            """)
    List<Object[]> countGroupedBySessionIds(@Param("sessionIds") List<Long> sessionIds);
}
