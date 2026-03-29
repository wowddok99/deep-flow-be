package com.deepflow.application.achievement;

import com.deepflow.application.port.out.notification.AchievementNotifier;
import com.deepflow.domain.achievement.Achievement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 세션 시작 시 시간 기반 칭호 체크를 정확한 타이밍에 예약하는 스케줄러.
 * 폴링 대신 임계치 시점에 맞춰 1회 실행하므로 딜레이가 거의 없음.
 */
@Slf4j
@Component
public class SessionTimeScheduler {

    private final AchievementService achievementService;
    private final AchievementNotifier achievementNotifier;
    private final ScheduledExecutorService executor;

    // 세션별 예약된 Future 목록 (세션 종료 시 취소용)
    private final Map<Long, List<ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>();

    /** 칭호 체크 예약 시점(초) */
    private static final long[] CHECK_POINTS = {
            10,      // D-00
            300,     // D-01 (5분)
            900,     // D-02 (15분)
            1_800,   // D-03 (30분)
            3_600,   // D-04 (1시간)
            7_200,   // D-05 (2시간)
            10_800,  // D-06 (3시간)
            14_400,  // D-07 (4시간)
            18_000,  // D-08 (5시간)
    };

    public SessionTimeScheduler(AchievementService achievementService,
                                AchievementNotifier achievementNotifier) {
        this.achievementService = achievementService;
        this.achievementNotifier = achievementNotifier;
        this.executor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "time-achievement");
            t.setDaemon(true);
            return t;
        });
    }

    /** 세션 시작 시 호출하여 각 임계치 시점에 칭호 체크 예약 */
    public void scheduleForSession(Long userId, Long sessionId) {
        List<ScheduledFuture<?>> futures = new CopyOnWriteArrayList<>();

        for (long checkPoint : CHECK_POINTS) {
            ScheduledFuture<?> future = executor.schedule(
                    () -> checkAchievement(userId, sessionId),
                    checkPoint,
                    TimeUnit.SECONDS
            );
            futures.add(future);
        }

        scheduledTasks.put(sessionId, futures);
        log.debug("시간 칭호 체크 예약: sessionId={}, checkPoints={}개", sessionId, CHECK_POINTS.length);
    }

    /** 세션 종료 시 호출하여 남은 예약 취소 */
    public void cancelForSession(Long sessionId) {
        List<ScheduledFuture<?>> futures = scheduledTasks.remove(sessionId);
        if (futures != null) {
            futures.forEach(f -> f.cancel(false));
            log.debug("시간 칭호 체크 취소: sessionId={}", sessionId);
        }
    }

    private void checkAchievement(Long userId, Long sessionId) {
        try {
            List<Achievement> granted = achievementService.checkAndGrant(
                    userId, sessionId, TriggerType.TIME_CHECK);

            if (!granted.isEmpty()) {
                achievementNotifier.notifyNewAchievements(userId, granted);
                log.info("실시간 시간 칭호 달성: userId={}, achievements={}",
                        userId, granted.stream().map(Achievement::getCode).toList());
            }
        } catch (Exception e) {
            log.error("Time check failed: sessionId={}", sessionId, e);
        }
    }
}
