package com.deepflow.infra.persistence.session;

import com.deepflow.domain.session.reaction.ReactionEmoji;
import com.deepflow.domain.session.reaction.SessionReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SessionReactionJpaRepository extends JpaRepository<SessionReaction, Long> {

    Optional<SessionReaction> findBySessionIdAndUserIdAndEmoji(Long sessionId, Long userId, ReactionEmoji emoji);

    int countBySessionIdAndEmoji(Long sessionId, ReactionEmoji emoji);

    @Query("""
            SELECT sr.sessionId AS sessionId, COUNT(sr) AS cnt
            FROM SessionReaction sr
            WHERE sr.sessionId IN :sessionIds
            GROUP BY sr.sessionId
            """)
    List<Object[]> countGroupedBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    @Query("""
            SELECT sr.emoji AS emoji, COUNT(sr) AS cnt
            FROM SessionReaction sr
            WHERE sr.sessionId = :sessionId
            GROUP BY sr.emoji
            """)
    List<Object[]> aggregateBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT sr.emoji FROM SessionReaction sr WHERE sr.sessionId = :sessionId AND sr.userId = :userId")
    List<ReactionEmoji> findEmojisBySessionIdAndUserId(@Param("sessionId") Long sessionId,
                                                      @Param("userId") Long userId);
}
