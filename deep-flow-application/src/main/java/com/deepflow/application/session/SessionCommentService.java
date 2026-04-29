package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.CommentAccessDeniedException;
import com.deepflow.application.exception.session.CommentNotFoundException;
import com.deepflow.application.exception.session.CommentParentMismatchException;
import com.deepflow.application.exception.session.SessionNotFoundException;
import com.deepflow.application.port.out.persistence.CommentMentionRepository;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionCommentRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.application.session.dto.CommentInfo;
import com.deepflow.application.session.dto.CommentInfo.MentionedUser;
import com.deepflow.application.session.dto.CreateCommentCommand;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.comment.CommentMention;
import com.deepflow.domain.session.comment.SessionComment;
import com.deepflow.domain.session.event.SessionCommentCreatedEvent;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionCommentService {

    private static final int CONTENT_PREVIEW_LIMIT = 100;

    private final SessionCommentRepository commentRepository;
    private final CommentMentionRepository mentionRepository;
    private final SessionRepository sessionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentInfo create(Long userId, Long sessionId, CreateCommentCommand cmd) {
        FocusSession session = requireSharedSessionWithMembership(sessionId, userId);

        if (cmd.parentId() != null) {
            SessionComment parent = commentRepository.findById(cmd.parentId())
                    .orElseThrow(CommentNotFoundException::new);
            if (!parent.getSessionId().equals(sessionId)) {
                throw new CommentParentMismatchException();
            }
        }

        User author = userRepository.findById(userId).orElseThrow(SessionNotFoundException::new);
        SessionComment c = SessionComment.create(sessionId, cmd.parentId(), author, cmd.content());
        commentRepository.save(c);

        List<Long> dedupedMentions = dedupeMentions(cmd.mentions(), userId);
        for (Long mentionedUserId : dedupedMentions) {
            mentionRepository.save(CommentMention.create(c.getId(), mentionedUserId));
        }

        eventPublisher.publishEvent(new SessionCommentCreatedEvent(
                c.getId(),
                sessionId,
                userId,
                dedupedMentions,
                truncate(cmd.content())
        ));
        log.info("댓글 작성: commentId={}, sessionId={}, userId={}, mentions={}",
                c.getId(), sessionId, userId, dedupedMentions.size());

        return CommentInfo.singleNonTree(c);
    }

    @Transactional
    public CommentInfo update(Long userId, Long commentId, String newContent) {
        SessionComment c = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        if (c.isDeleted() || !c.isOwnedBy(userId)) {
            throw new CommentAccessDeniedException();
        }
        c.edit(newContent);
        return CommentInfo.singleNonTree(c);
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        SessionComment c = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        if (c.isDeleted() || !c.isOwnedBy(userId)) {
            throw new CommentAccessDeniedException();
        }
        c.softDelete(LocalDateTime.now());
    }

    public List<CommentInfo> getComments(Long userId, Long sessionId) {
        requireSharedSessionWithMembership(sessionId, userId);

        List<SessionComment> all = commentRepository.findAllBySessionIdWithUser(sessionId);
        if (all.isEmpty()) return List.of();

        Map<Long, List<SessionComment>> childrenByParent = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(SessionComment::getParentId));

        Map<Long, List<MentionedUser>> mentionsByCommentId = loadMentionsByCommentId(
                all.stream().map(SessionComment::getId).toList());

        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> CommentInfo.fromTree(c, childrenByParent, mentionsByCommentId))
                .toList();
    }

    /**
     * 댓글 ID 묶음으로 멘션을 batch 조회한 뒤, 멘션된 사용자 정보(username, name)를 한 번 더
     * batch 로 조회해 commentId → 멘션 사용자 목록 으로 묶는다.
     * 댓글 본문에서 '@username' 매칭으로 강조 표시할 때 정확한 사용자만 chip 스타일로 렌더된다.
     */
    private Map<Long, List<MentionedUser>> loadMentionsByCommentId(List<Long> commentIds) {
        if (commentIds.isEmpty()) return Map.of();

        List<CommentMention> mentions = mentionRepository.findByCommentIds(commentIds);
        if (mentions.isEmpty()) return Map.of();

        List<Long> userIds = mentions.stream()
                .map(CommentMention::getUserId)
                .distinct()
                .toList();
        Map<Long, User> userById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return mentions.stream()
                .filter(m -> userById.containsKey(m.getUserId()))
                .collect(Collectors.groupingBy(
                        CommentMention::getCommentId,
                        Collectors.mapping(
                                m -> {
                                    User u = userById.get(m.getUserId());
                                    return new MentionedUser(u.getId(), u.getUsername(), u.getName());
                                },
                                Collectors.toList()
                        )
                ));
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

    private List<Long> dedupeMentions(List<Long> raw, Long actorUserId) {
        if (raw == null || raw.isEmpty()) return List.of();
        Set<Long> seen = new HashSet<>();
        return raw.stream()
                .filter(java.util.Objects::nonNull)
                .filter(uid -> !uid.equals(actorUserId))
                .filter(seen::add)
                .toList();
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() <= CONTENT_PREVIEW_LIMIT ? s : s.substring(0, CONTENT_PREVIEW_LIMIT);
    }
}
