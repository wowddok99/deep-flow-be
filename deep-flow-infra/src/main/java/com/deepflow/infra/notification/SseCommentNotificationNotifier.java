package com.deepflow.infra.notification;

import com.deepflow.application.port.out.notification.CommentNotificationNotifier;
import com.deepflow.application.session.comment.dto.CommentNotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseCommentNotificationNotifier implements CommentNotificationNotifier {

    private final SseEmitterManager sseEmitterManager;

    @Override
    public void notifyTo(Long userId, CommentNotificationPayload payload) {
        if (!sseEmitterManager.isConnected(userId, SseEmitterManager.Channel.COMMENT_NOTIFICATION)) return;
        sseEmitterManager.send(userId, SseEmitterManager.Channel.COMMENT_NOTIFICATION, "comment-notification", payload);
    }
}
