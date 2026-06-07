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

    /**
     * 세션 종료 후 통계와 칭호 후처리
     *
     * 세션 종료 저장이 확정된 뒤 실행해 후처리 실패가 종료 흐름을 막지 않도록 분리
     */
    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionStoppedEvent(SessionStoppedEvent event) {
        log.info("Processing session stopped event: sessionId={}, userId={}, duration={}s",
                event.getSessionId(), event.getUserId(), event.getDurationSeconds());

        try {
            dailyFocusStatsService.upsertStats(event.getUserId(), event.getStartTime(), event.getEndTime());
        } catch (Exception e) {
            log.error("Failed to update daily stats for session {}", event.getSessionId(), e);
        }

        try {
            List<Achievement> grantedAchievements = achievementService.checkAndGrant(
                    event.getUserId(), event.getSessionId(), TriggerType.SESSION_STOP);

            if (!grantedAchievements.isEmpty()) {
                achievementNotifier.notifyNewAchievements(event.getUserId(), grantedAchievements);
            }
        } catch (Exception e) {
            log.error("Failed to check achievements for session {}", event.getSessionId(), e);
        }

        log.info("Completed processing for session {}", event.getSessionId());
    }

    /**
     * 로그 수정 후 칭호 후처리
     *
     * 로그 수정 결과가 확정된 뒤 실행해 변경된 기록 기준으로 칭호 조건을 확인
     */
    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLogUpdatedEvent(LogUpdatedEvent event) {
        log.info("Processing log updated event: sessionId={}, userId={}",
                event.getSessionId(), event.getUserId());

        try {
            List<Achievement> grantedAchievements = achievementService.checkAndGrant(
                    event.getUserId(), event.getSessionId(), TriggerType.LOG_UPDATE);

            if (!grantedAchievements.isEmpty()) {
                achievementNotifier.notifyNewAchievements(event.getUserId(), grantedAchievements);
            }
        } catch (Exception e) {
            log.error("Failed to check achievements on log update for session {}",
                    event.getSessionId(), e);
        }
    }
}
