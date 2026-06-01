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

    int countByEmoji(Long sessionId, ReactionEmoji emoji);

    /**
     * 피드 목록 조립에서 세션별 리액션 수 조회 N+1 방지
     */
    Map<Long, Integer> countBySessionIds(List<Long> sessionIds);

    List<EmojiCount> aggregateBySession(Long sessionId);

    List<ReactionEmoji> findReactedEmojisByUser(Long sessionId, Long userId);

    /**
     * 이모지별 반응자 목록이 생성 순서대로 안정적으로 보이도록 createdAt 오름차순 조회
     */
    List<SessionReaction> findAllBySession(Long sessionId);

    record EmojiCount(ReactionEmoji emoji, int count) {}
}
