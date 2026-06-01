package com.deepflow.infra.notification;

import com.deepflow.application.port.out.notification.AchievementNotifier;
import com.deepflow.domain.achievement.Achievement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SseAchievementNotifier implements AchievementNotifier {

    private final SseEmitterManager sseEmitterManager;

    @Override
    public void notifyNewAchievements(Long userId, List<Achievement> achievements) {
        for (Achievement achievement : achievements) {
            sseEmitterManager.send(userId, SseEmitterManager.Channel.ACHIEVEMENT, "achievement", Map.of(
                    "code", achievement.getCode(),
                    "name", achievement.getName(),
                    "description", achievement.getDescription(),
                    "category", achievement.getCategory().name(),
                    "grade", achievement.getGrade()
            ));
        }
    }
}
