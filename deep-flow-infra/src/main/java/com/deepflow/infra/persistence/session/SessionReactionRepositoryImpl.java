package com.deepflow.infra.persistence.session;

import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.domain.session.reaction.ReactionEmoji;
import com.deepflow.domain.session.reaction.SessionReaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SessionReactionRepositoryImpl implements SessionReactionRepository {

    private final SessionReactionJpaRepository jpa;

    @Override
    public Optional<SessionReaction> find(Long sessionId, Long userId, ReactionEmoji emoji) {
        return jpa.findBySessionIdAndUserIdAndEmoji(sessionId, userId, emoji);
    }

    @Override
    public SessionReaction save(SessionReaction reaction) {
        return jpa.save(reaction);
    }

    @Override
    public void delete(Long reactionId) {
        jpa.deleteById(reactionId);
    }

    @Override
    public int countByEmoji(Long sessionId, ReactionEmoji emoji) {
        return jpa.countBySessionIdAndEmoji(sessionId, emoji);
    }

    @Override
    public Map<Long, Integer> countBySessionIds(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return Map.of();
        Map<Long, Integer> result = new HashMap<>();
        for (Object[] row : jpa.countGroupedBySessionIds(sessionIds)) {
            result.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return result;
    }

    @Override
    public List<EmojiCount> aggregateBySession(Long sessionId) {
        return jpa.aggregateBySessionId(sessionId).stream()
                .map(row -> new EmojiCount((ReactionEmoji) row[0], ((Number) row[1]).intValue()))
                .toList();
    }

    @Override
    public List<ReactionEmoji> findReactedEmojisByUser(Long sessionId, Long userId) {
        return jpa.findEmojisBySessionIdAndUserId(sessionId, userId);
    }
}
