package com.deepflow.infra.notification;

import com.deepflow.application.port.out.notification.AchievementStreamManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SSE 연결 생성을 SseEmitterManager에 위임 (AchievementStreamManager 구현체) */
@Component
@RequiredArgsConstructor
public class SseAchievementStreamManager implements AchievementStreamManager {

    private final SseEmitterManager sseEmitterManager;

    @Override
    public Object connect(Long userId) {
        return sseEmitterManager.connect(userId, SseEmitterManager.Channel.ACHIEVEMENT);
    }
}
