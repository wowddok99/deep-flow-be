package com.deepflow.application.session.reaction;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.InvalidReactionEmojiException;
import com.deepflow.application.exception.session.SessionNotFoundException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.application.session.reaction.dto.ReactionAggregateInfo;
import com.deepflow.application.session.reaction.dto.ReactionAggregateInfo.EmojiCountInfo;
import com.deepflow.application.session.reaction.dto.ReactionAggregateInfo.ReactorSummary;
import com.deepflow.application.session.reaction.dto.ReactionToggleResult;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.event.SessionReactionAddedEvent;
import com.deepflow.domain.session.event.SessionReactionRemovedEvent;
import com.deepflow.domain.session.reaction.ReactionEmoji;
import com.deepflow.domain.session.reaction.SessionReaction;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionReactionService {

    private static final int TOP_REACTORS_LIMIT = 5;

    private final SessionRepository sessionRepository;
    private final SessionReactionRepository reactionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 공유 세션 리액션 토글
     * 이미 누른 리액션이면 제거하고, 없으면 추가
     */
    @Transactional
    public ReactionToggleResult toggle(Long userId, Long sessionId, String emojiUnicode) {
        ReactionEmoji emoji = ReactionEmoji.fromUnicode(emojiUnicode);
        if (emoji == null) {
            throw new InvalidReactionEmojiException();
        }

        validateSharedSessionMembership(sessionId, userId);

        Optional<SessionReaction> existingReaction = reactionRepository.find(sessionId, userId, emoji);

        boolean added;
        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get().getId());

            SessionReactionRemovedEvent event = new SessionReactionRemovedEvent(
                    sessionId,
                    userId,
                    emoji);

            eventPublisher.publishEvent(event);
            added = false;
        } else {
            SessionReaction reaction = SessionReaction.of(sessionId, userId, emoji);
            reactionRepository.save(reaction);

            SessionReactionAddedEvent event = new SessionReactionAddedEvent(
                    sessionId,
                    userId,
                    emoji);

            eventPublisher.publishEvent(event);
            added = true;
        }

        int totalCount = reactionRepository.countByEmoji(sessionId, emoji);

        log.info("리액션 토글: sessionId={}, userId={}, emoji={}, added={}, total={}",
                sessionId, userId, emoji, added, totalCount);

        return new ReactionToggleResult(emoji.unicode(), added, totalCount, added);
    }

    /**
     * 공유 세션 리액션 집계 조회
     * 이모지별 개수, 내 리액션 여부, 대표 리액터 목록을 구성
     */
    public ReactionAggregateInfo aggregate(Long userId, Long sessionId) {
        validateSharedSessionMembership(sessionId, userId);

        List<SessionReaction> reactions = reactionRepository.findAllBySession(sessionId);
        Map<Long, User> userById = loadReactorUsers(reactions);

        Map<ReactionEmoji, List<SessionReaction>> reactionsByEmoji = reactions.stream()
                .collect(Collectors.groupingBy(SessionReaction::getEmoji));

        List<EmojiCountInfo> items = Arrays.stream(ReactionEmoji.values())
                .map(emoji -> buildEmojiCountInfo(
                        emoji,
                        reactionsByEmoji.getOrDefault(emoji, List.of()),
                        userById,
                        userId))
                .toList();

        return new ReactionAggregateInfo(items);
    }

    private EmojiCountInfo buildEmojiCountInfo(ReactionEmoji emoji,
                                               List<SessionReaction> reactionsForEmoji,
                                               Map<Long, User> userById,
                                               Long viewerUserId) {
        boolean viewerReacted = reactionsForEmoji.stream()
                .anyMatch(reaction -> reaction.getUserId().equals(viewerUserId));

        List<ReactorSummary> topReactors = new ArrayList<>(TOP_REACTORS_LIMIT);
        for (SessionReaction reaction : reactionsForEmoji) {
            if (topReactors.size() >= TOP_REACTORS_LIMIT) {
                break;
            }

            User reactor = userById.get(reaction.getUserId());
            if (reactor == null) {
                continue;
            }

            topReactors.add(new ReactorSummary(reactor.getId(), reactor.getName()));
        }

        return new EmojiCountInfo(
                emoji.unicode(),
                reactionsForEmoji.size(),
                viewerReacted,
                topReactors);
    }

    private Map<Long, User> loadReactorUsers(List<SessionReaction> reactions) {
        if (reactions.isEmpty()) {
            return Map.of();
        }

        List<Long> userIds = reactions.stream()
                .map(SessionReaction::getUserId)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private void validateSharedSessionMembership(Long sessionId, Long userId) {
        FocusSession session = sessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        if (!session.isShared() || session.getDeletedAt() != null) {
            throw new SessionNotFoundException();
        }

        if (!crewMemberRepository.existsByCrewIdAndUserId(session.getSharedCrewId(), userId)) {
            throw new NotCrewMemberException();
        }
    }
}
