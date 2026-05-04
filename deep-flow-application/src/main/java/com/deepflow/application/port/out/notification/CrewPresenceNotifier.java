package com.deepflow.application.port.out.notification;

import com.deepflow.application.crew.dto.CrewPresencePayload;

import java.util.List;

public interface CrewPresenceNotifier {
    void broadcastToUsers(List<Long> userIds, CrewPresencePayload payload);
}
