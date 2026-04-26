package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.InvalidReactionEmojiException;
import com.deepflow.application.exception.session.SessionNotFoundException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.session.dto.ReactionAggregateInfo;
import com.deepflow.application.session.dto.ReactionToggleResult;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.event.SessionReactionAddedEvent;
import com.deepflow.domain.session.event.SessionReactionRemovedEvent;
import com.deepflow.domain.session.reaction.ReactionEmoji;
import com.deepflow.domain.session.reaction.SessionReaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionReactionService {

    private final SessionRepository sessionRepository;
    private final SessionReactionRepository reactionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReactionToggleResult toggle(Long userId, Long sessionId, String emojiUnicode) {
        ReactionEmoji emoji = ReactionEmoji.fromUnicode(emojiUnicode);
        if (emoji == null) throw new InvalidReactionEmojiException();

        FocusSession session = requireSharedSessionWithMembership(sessionId, userId);

        Optional<SessionReaction> existing = reactionRepository.find(sessionId, userId, emoji);
        boolean added;
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get().getId());
            eventPublisher.publishEvent(new SessionReactionRemovedEvent(sessionId, userId, emoji));
            added = false;
        } else {
            reactionRepository.save(SessionReaction.of(sessionId, userId, emoji));
            eventPublisher.publishEvent(new SessionReactionAddedEvent(sessionId, userId, emoji));
            added = true;
        }

        int totalCount = reactionRepository.countByEmoji(sessionId, emoji);
        log.info("리액션 토글: sessionId={}, userId={}, emoji={}, added={}, total={}",
                sessionId, userId, emoji, added, totalCount);

        return new ReactionToggleResult(emoji.unicode(), added, totalCount, added);
    }

    public ReactionAggregateInfo aggregate(Long userId, Long sessionId) {
        FocusSession session = requireSharedSessionWithMembership(sessionId, userId);

        Map<ReactionEmoji, Integer> counts = new EnumMap<>(ReactionEmoji.class);
        for (SessionReactionRepository.EmojiCount ec : reactionRepository.aggregateBySession(sessionId)) {
            counts.put(ec.emoji(), ec.count());
        }
        Set<ReactionEmoji> userReacted = Set.copyOf(reactionRepository.findReactedEmojisByUser(sessionId, userId));

        List<ReactionAggregateInfo.EmojiCountInfo> items = java.util.Arrays.stream(ReactionEmoji.values())
                .map(e -> new ReactionAggregateInfo.EmojiCountInfo(
                        e.unicode(),
                        counts.getOrDefault(e, 0),
                        userReacted.contains(e)))
                .toList();
        return new ReactionAggregateInfo(items);
    }

    private FocusSession requireSharedSessionWithMembership(Long sessionId, Long userId) {
        FocusSession session = sessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
        if (!session.isShared() || session.getDeletedAt() != null) {
            throw new SessionNotFoundException();
        }
        if (!crewMemberRepository.existsByCrewIdAndUserId(session.getSharedCrewId(), userId)) {
            throw new NotCrewMemberException();
        }
        return session;
    }
}
