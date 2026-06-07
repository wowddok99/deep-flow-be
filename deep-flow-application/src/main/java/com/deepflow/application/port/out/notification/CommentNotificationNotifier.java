package com.deepflow.application.port.out.notification;

import com.deepflow.application.session.comment.dto.CommentNotificationPayload;

public interface CommentNotificationNotifier {

    void notifyTo(Long userId, CommentNotificationPayload payload);
}
