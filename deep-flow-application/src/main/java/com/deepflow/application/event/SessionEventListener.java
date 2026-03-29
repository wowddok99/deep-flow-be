package com.deepflow.application.event;

import com.deepflow.application.achievement.AchievementService;
import com.deepflow.application.achievement.TriggerType;
import com.deepflow.application.port.out.notification.AchievementNotifier;
import com.deepflow.application.stats.DailyFocusStatsService;
import com.deepflow.domain.achievement.Achievement;
import com.deepflow.domain.session.event.LogUpdatedEvent;
import com.deepflow.domain.session.event.SessionStoppedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {

    private final DailyFocusStatsService dailyFocusStatsService;
    private final AchievementService achievementService;
    private final AchievementNotifier achievementNotifier;

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionStoppedEvent(SessionStoppedEvent event) {
        log.info("Processing session stopped event: sessionId={}, userId={}, duration={}s",
                event.getSessionId(), event.getUserId(), event.getDurationSeconds());

        // 순서 중요: 통계가 먼저 커밋되어야 칭호 평가 시 오늘 통계가 반영됨
        try {
            dailyFocusStatsService.upsertStats(event.getUserId(), event.getDurationSeconds());
        } catch (Exception e) {
            log.error("Failed to update daily stats for session {}", event.getSessionId(), e);
        }

        try {
            List<Achievement> granted = achievementService.checkAndGrant(
                    event.getUserId(), event.getSessionId(), TriggerType.SESSION_STOP);

            if (!granted.isEmpty()) {
                achievementNotifier.notifyNewAchievements(event.getUserId(), granted);
            }
        } catch (Exception e) {
            log.error("Failed to check achievements for session {}", event.getSessionId(), e);
        }

        log.info("Completed processing for session {}", event.getSessionId());
    }

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLogUpdatedEvent(LogUpdatedEvent event) {
        log.info("Processing log updated event: sessionId={}, userId={}",
                event.getSessionId(), event.getUserId());

        try {
            List<Achievement> granted = achievementService.checkAndGrant(
                    event.getUserId(), event.getSessionId(), TriggerType.LOG_UPDATE);

            if (!granted.isEmpty()) {
                achievementNotifier.notifyNewAchievements(event.getUserId(), granted);
            }
        } catch (Exception e) {
            log.error("Failed to check achievements on log update for session {}",
                    event.getSessionId(), e);
        }
    }
}
