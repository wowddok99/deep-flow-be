package com.deepflow.infra.notification;

import com.deepflow.application.crew.dto.CrewPresencePayload;
import com.deepflow.application.port.out.notification.CrewPresenceNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SseCrewPresenceNotifier implements CrewPresenceNotifier {

    private final SseEmitterManager sseEmitterManager;

    @Override
    public void broadcastToUsers(List<Long> userIds, CrewPresencePayload payload) {
        if (userIds == null || userIds.isEmpty()) return;
        for (Long userId : userIds) {
            if (sseEmitterManager.isConnected(userId)) {
                sseEmitterManager.send(userId, "crew-presence", payload);
            }
        }
    }
}
