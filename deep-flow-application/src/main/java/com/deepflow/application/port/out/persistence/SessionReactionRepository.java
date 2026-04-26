package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.reaction.ReactionEmoji;
import com.deepflow.domain.session.reaction.SessionReaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SessionReactionRepository {

    Optional<SessionReaction> find(Long sessionId, Long userId, ReactionEmoji emoji);

    SessionReaction save(SessionReaction reaction);

    void delete(Long reactionId);

    /**
     * 세션의 특정 이모지 총 카운트.
     */
    int countByEmoji(Long sessionId, ReactionEmoji emoji);

    /**
     * 피드 batch 카운트 — sessionId 별 총 리액션 수 (이모지 무관).
     */
    Map<Long, Integer> countBySessionIds(List<Long> sessionIds);

    /**
     * 세션의 이모지별 카운트 응답용.
     */
    List<EmojiCount> aggregateBySession(Long sessionId);

    /**
     * 세션에서 본인이 누른 이모지 목록.
     */
    List<ReactionEmoji> findReactedEmojisByUser(Long sessionId, Long userId);

    record EmojiCount(ReactionEmoji emoji, int count) {}
}
