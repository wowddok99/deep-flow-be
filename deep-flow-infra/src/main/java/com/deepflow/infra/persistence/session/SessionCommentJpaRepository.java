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

    @Query("""
            SELECT c.sessionId AS sessionId, COUNT(c) AS cnt
            FROM SessionComment c
            WHERE c.sessionId IN :sessionIds
              AND c.deletedAt IS NULL
            GROUP BY c.sessionId
            """)
    List<Object[]> countGroupedBySessionIds(@Param("sessionIds") List<Long> sessionIds);
}
