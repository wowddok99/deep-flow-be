package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.InvalidReactionEmojiException;
import com.deepflow.application.exception.session.SessionNotFoundException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.application.session.dto.ReactionAggregateInfo;
import com.deepflow.application.session.dto.ReactionAggregateInfo.EmojiCountInfo;
import com.deepflow.application.session.dto.ReactionAggregateInfo.ReactorSummary;
import com.deepflow.application.session.dto.ReactionToggleResult;
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
        requireSharedSessionWithMembership(sessionId, userId);

        List<SessionReaction> reactions = reactionRepository.findAllBySession(sessionId);
        Map<Long, User> userById = loadReactorUsers(reactions);

        Map<ReactionEmoji, List<SessionReaction>> groupedByEmoji = reactions.stream()
                .collect(Collectors.groupingBy(SessionReaction::getEmoji));

        List<EmojiCountInfo> items = Arrays.stream(ReactionEmoji.values())
                .map(emoji -> buildItem(emoji, groupedByEmoji.getOrDefault(emoji, List.of()), userById, userId))
                .toList();

        return new ReactionAggregateInfo(items);
    }

    private EmojiCountInfo buildItem(ReactionEmoji emoji,
                                     List<SessionReaction> reactionsForEmoji,
                                     Map<Long, User> userById,
                                     Long viewerUserId) {
        boolean userReacted = reactionsForEmoji.stream()
                .anyMatch(r -> r.getUserId().equals(viewerUserId));

        List<ReactorSummary> topReactors = new ArrayList<>(TOP_REACTORS_LIMIT);
        for (SessionReaction r : reactionsForEmoji) {
            if (topReactors.size() >= TOP_REACTORS_LIMIT) break;
            User u = userById.get(r.getUserId());
            if (u == null) continue;
            topReactors.add(new ReactorSummary(u.getId(), u.getName()));
        }

        return new EmojiCountInfo(emoji.unicode(), reactionsForEmoji.size(), userReacted, topReactors);
    }

    private Map<Long, User> loadReactorUsers(List<SessionReaction> reactions) {
        if (reactions.isEmpty()) return Map.of();
        List<Long> userIds = reactions.stream()
                .map(SessionReaction::getUserId)
                .distinct()
                .toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
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
