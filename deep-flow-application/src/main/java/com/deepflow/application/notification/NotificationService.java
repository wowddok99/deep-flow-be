package com.deepflow.application.notification;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.exception.session.NotificationAccessDeniedException;
import com.deepflow.application.exception.session.NotificationNotFoundException;
import com.deepflow.application.port.out.persistence.CommentMentionRepository;
import com.deepflow.application.port.out.persistence.SessionCommentRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.notification.dto.MentionInfo;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.comment.CommentMention;
import com.deepflow.domain.session.comment.SessionComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final CommentMentionRepository mentionRepository;
    private final SessionCommentRepository commentRepository;
    private final SessionRepository sessionRepository;

    /**
     * 미읽음 멘션 알림을 이동 경로 정보와 함께 조회
     *
     * 멘션, 댓글, 세션을 한 번에 묶어 알림 목록 조립 시 N+1 조회 방지
     */
    public SliceResult<MentionInfo> getUnread(Long userId, Long cursorId, int size) {
        SliceResult<CommentMention> slice = mentionRepository.findUnreadByUserId(userId, cursorId, size);
        List<CommentMention> mentions = slice.content();

        if (mentions.isEmpty()) {
            return new SliceResult<>(List.of(), slice.nextCursorId(), slice.hasNext());
        }

        List<Long> commentIds = mentions.stream().map(CommentMention::getCommentId).distinct().toList();
        Map<Long, SessionComment> commentMap = commentRepository.findAllByIdsWithUser(commentIds).stream()
                .collect(Collectors.toMap(SessionComment::getId, Function.identity()));

        List<Long> sessionIds = commentMap.values().stream()
                .map(SessionComment::getSessionId)
                .distinct()
                .toList();
        Map<Long, FocusSession> sessionMap = sessionRepository.findAllByIds(sessionIds).stream()
                .collect(Collectors.toMap(FocusSession::getId, Function.identity()));

        List<MentionInfo> infos = mentions.stream()
                .map(m -> {
                    SessionComment comment = commentMap.get(m.getCommentId());
                    FocusSession session = (comment != null) ? sessionMap.get(comment.getSessionId()) : null;
                    return MentionInfo.of(m, comment, session);
                })
                .toList();

        return new SliceResult<>(infos, slice.nextCursorId(), slice.hasNext());
    }

    @Transactional
    public void markRead(Long userId, Long mentionId) {
        CommentMention m = mentionRepository.findById(mentionId).orElseThrow(NotificationNotFoundException::new);
        if (!m.getUserId().equals(userId)) throw new NotificationAccessDeniedException();
        m.markRead(LocalDateTime.now());
    }

    @Transactional
    public int markAllRead(Long userId) {
        int updated = mentionRepository.markAllReadByUser(userId, LocalDateTime.now());
        log.info("알림 일괄 읽음: userId={}, count={}", userId, updated);
        return updated;
    }
}
