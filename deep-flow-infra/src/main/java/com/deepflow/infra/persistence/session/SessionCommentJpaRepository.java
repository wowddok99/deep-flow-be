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
     * 댓글 ID 목록으로 batch 조회 (User fetch join). 알림 deep link 매핑용.
     * soft-delete 된 댓글은 제외 — 멘션은 살아있어도 댓글이 사라졌다면
     * unread 알림으로 보여줘 봤자 "삭제된 댓글입니다" 만 노출되어 헛걸음.
     * (cascade 로 mention row 자체를 정리하는 후속 과제 TASK-002 참조)
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
