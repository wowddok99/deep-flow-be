package com.deepflow.application.session.comment;

import com.deepflow.application.port.out.notification.CommentNotificationNotifier;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.application.session.comment.dto.CommentNotificationPayload;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.event.SessionCommentCreatedEvent;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationListener {

    private final CommentNotificationNotifier notifier;
    private final SessionRepository sessionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    // 댓글 저장이 확정된 뒤 알림을 보내야 알림 클릭 시 이동할 댓글이 항상 존재
    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(SessionCommentCreatedEvent e) {
        FocusSession session = sessionRepository.findById(e.getSessionId()).orElse(null);
        if (session == null || session.getSharedCrewId() == null) return;

        User actor = userRepository.findById(e.getActorUserId()).orElse(null);
        String actorName = actor == null ? "알수없음" : actor.getName();

        Long ownerId = session.getUser() != null ? session.getUser().getId() : null;
        boolean ownerNotified = false;
        // 작성자 본인 댓글은 내 글 댓글 알림에서 제외
        if (ownerId != null && !ownerId.equals(e.getActorUserId())) {
            notifier.notifyTo(ownerId, payload(
                    CommentNotificationPayload.Type.COMMENT_ON_YOUR_POST,
                    e, actorName));
            ownerNotified = true;
        }

        if (e.getMentionedUserIds() == null || e.getMentionedUserIds().isEmpty()) return;

        Set<Long> crewMemberIds = crewMemberRepository.findUserIdsByCrewId(session.getSharedCrewId());
        for (Long uid : e.getMentionedUserIds()) {
            if (uid.equals(e.getActorUserId())) continue;
            if (!crewMemberIds.contains(uid)) continue;
            // 글 작성자가 이미 댓글 알림을 받았다면 같은 댓글의 멘션 알림은 중복 발송하지 않음
            if (ownerNotified && uid.equals(ownerId)) continue;
            notifier.notifyTo(uid, payload(
                    CommentNotificationPayload.Type.MENTION,
                    e, actorName));
        }

        log.info("댓글 알림 발송: commentId={}, sessionId={}, actor={}, mentions={}",
                e.getCommentId(), e.getSessionId(), e.getActorUserId(),
                e.getMentionedUserIds().size());
    }

    private CommentNotificationPayload payload(CommentNotificationPayload.Type type,
                                                SessionCommentCreatedEvent e,
                                                String actorName) {
        return new CommentNotificationPayload(
                type,
                e.getSessionId(),
                e.getCommentId(),
                e.getActorUserId(),
                actorName,
                e.getContentPreview()
        );
    }
}
