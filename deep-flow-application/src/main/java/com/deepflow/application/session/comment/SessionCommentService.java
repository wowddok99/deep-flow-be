package com.deepflow.application.session.comment;

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
import com.deepflow.application.session.comment.dto.CommentInfo;
import com.deepflow.application.session.comment.dto.CommentInfo.MentionedUser;
import com.deepflow.application.session.comment.dto.CreateCommentCommand;
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

    /**
     * 공유 세션 댓글 작성
     * 댓글 저장 이후 멘션 알림 처리는 이벤트로 분리
     */
    @Transactional
    public CommentInfo create(Long userId, Long sessionId, CreateCommentCommand command) {
        validateSharedSessionMembership(sessionId, userId);

        if (command.parentId() != null) {
            SessionComment parentComment = commentRepository.findById(command.parentId())
                    .orElseThrow(CommentNotFoundException::new);

            // 다른 세션의 댓글을 부모로 연결하지 못하도록 댓글 트리 소속 검증
            if (!parentComment.getSessionId().equals(sessionId)) {
                throw new CommentParentMismatchException();
            }
        }

        User author = userRepository.findById(userId)
                .orElseThrow(SessionNotFoundException::new);

        SessionComment comment = SessionComment.create(
                sessionId,
                command.parentId(),
                author,
                command.content());

        commentRepository.save(comment);

        // 요청 멘션 목록에서 자기 자신, null, 중복 사용자를 제거
        List<Long> mentionedUserIds = extractMentionedUserIds(command.mentions(), userId);

        for (Long mentionedUserId : mentionedUserIds) {
            mentionRepository.save(CommentMention.create(comment.getId(), mentionedUserId));
        }

        SessionCommentCreatedEvent event = new SessionCommentCreatedEvent(
                comment.getId(),
                sessionId,
                userId,
                mentionedUserIds,
                truncate(command.content()));

        eventPublisher.publishEvent(event);

        log.info("댓글 작성: commentId={}, sessionId={}, userId={}, mentions={}",
                comment.getId(), sessionId, userId, mentionedUserIds.size());

        return CommentInfo.singleNonTree(comment);
    }

    @Transactional
    public CommentInfo update(Long userId, Long commentId, String newContent) {
        SessionComment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        if (comment.isDeleted() || !comment.isOwnedBy(userId)) {
            throw new CommentAccessDeniedException();
        }

        comment.edit(newContent);

        return CommentInfo.singleNonTree(comment);
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        SessionComment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        if (comment.isDeleted() || !comment.isOwnedBy(userId)) {
            throw new CommentAccessDeniedException();
        }

        comment.softDelete(LocalDateTime.now());
    }

    public List<CommentInfo> getComments(Long userId, Long sessionId) {
        validateSharedSessionMembership(sessionId, userId);

        List<SessionComment> comments = commentRepository.findAllBySessionIdWithUser(sessionId);
        if (comments.isEmpty()) {
            return List.of();
        }

        // 노출 가능한 대댓글을 부모 ID 기준으로 그룹화
        Map<Long, List<SessionComment>> childrenByParent = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .filter(comment -> !comment.isDeleted())
                .collect(Collectors.groupingBy(SessionComment::getParentId));

        // 댓글 본문에서 멘션 강조에 필요한 사용자 정보를 댓글 ID 기준으로 조회
        Map<Long, List<MentionedUser>> mentionsByCommentId = loadMentionsByCommentId(
                comments.stream()
                        .map(SessionComment::getId)
                        .toList());

        return comments.stream()
                .filter(comment -> comment.getParentId() == null)
                .map(comment -> CommentInfo.fromTree(comment, childrenByParent, mentionsByCommentId))
                .filter(commentInfo -> !(commentInfo.deleted() && commentInfo.replies().isEmpty()))
                .toList();
    }

    private Map<Long, List<MentionedUser>> loadMentionsByCommentId(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }

        List<CommentMention> mentions = mentionRepository.findByCommentIds(commentIds);
        if (mentions.isEmpty()) {
            return Map.of();
        }

        List<Long> userIds = mentions.stream()
                .map(CommentMention::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 조회되지 않는 사용자 멘션은 응답에서 제외
        return mentions.stream()
                .filter(mention -> userById.containsKey(mention.getUserId()))
                .collect(Collectors.groupingBy(
                        CommentMention::getCommentId,
                        Collectors.mapping(
                                mention -> {
                                    User user = userById.get(mention.getUserId());
                                    return new MentionedUser(
                                            user.getId(),
                                            user.getUsername(),
                                            user.getName());
                                },
                                Collectors.toList()
                        )
                ));
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

    private List<Long> extractMentionedUserIds(List<Long> rawMentions, Long actorUserId) {
        if (rawMentions == null || rawMentions.isEmpty()) {
            return List.of();
        }

        Set<Long> seen = new HashSet<>();

        // 자기 자신과 중복 멘션은 알림 대상에서 제외
        return rawMentions.stream()
                .filter(java.util.Objects::nonNull)
                .filter(userId -> !userId.equals(actorUserId))
                .filter(seen::add)
                .toList();
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }

        // 알림 미리보기에는 전체 댓글 대신 최대 길이까지만 포함
        return content.length() <= CONTENT_PREVIEW_LIMIT
                ? content
                : content.substring(0, CONTENT_PREVIEW_LIMIT);
    }
}
