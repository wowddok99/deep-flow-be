package com.deepflow.application.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.exception.session.NotificationAccessDeniedException;
import com.deepflow.application.exception.session.NotificationNotFoundException;
import com.deepflow.application.port.out.persistence.CommentMentionRepository;
import com.deepflow.application.session.dto.MentionInfo;
import com.deepflow.domain.session.comment.CommentMention;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final CommentMentionRepository mentionRepository;

    public SliceResult<MentionInfo> getUnread(Long userId, Long cursorId, int size) {
        SliceResult<CommentMention> slice = mentionRepository.findUnreadByUserId(userId, cursorId, size);
        return new SliceResult<>(
                slice.content().stream().map(MentionInfo::from).toList(),
                slice.nextCursorId(),
                slice.hasNext()
        );
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
