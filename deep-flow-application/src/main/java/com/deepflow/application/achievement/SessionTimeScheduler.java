package com.deepflow.application.achievement;

import com.deepflow.application.port.out.notification.AchievementNotifier;
import com.deepflow.domain.achievement.Achievement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 세션 시작 시 시간 기반 칭호 체크를 임계치 시점에 맞춰 예약
 *
 * 폴링 대신 예약 실행을 사용해 장시간 세션 칭호 반영 지연 최소화
 */
@Slf4j
@Component
public class SessionTimeScheduler {

    private final AchievementService achievementService;
    private final AchievementNotifier achievementNotifier;
    private final ScheduledExecutorService executor;

    // 세션 종료 시 남은 예약을 취소하기 위해 세션별 Future 보관
    private final Map<Long, List<ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>();

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

    public void scheduleForSession(Long userId, Long sessionId) {
        // 세션 종료 시 남은 예약을 취소하기 위해 작업 목록을 보관
        List<ScheduledFuture<?>> futures = new CopyOnWriteArrayList<>();

        for (long checkPoint : CHECK_POINTS) {
            // 각 임계 시점에 시간 기반 칭호 조건을 확인하도록 예약
            ScheduledFuture<?> future = executor.schedule(
                    () -> checkAchievement(userId, sessionId),
                    checkPoint,
                    TimeUnit.SECONDS
            );
            futures.add(future);
        }

        // 세션 종료 시 예약된 체크 작업을 일괄 취소하기 위해 세션 ID 기준으로 저장
        scheduledTasks.put(sessionId, futures);

        log.debug("시간 칭호 체크 예약: sessionId={}, checkPoints={}개", sessionId, CHECK_POINTS.length);
    }

    public void cancelForSession(Long sessionId) {
        List<ScheduledFuture<?>> futures = scheduledTasks.remove(sessionId);
        if (futures != null) {
            // 이미 실행 중인 체크는 중단하지 않고, 아직 대기 중인 예약만 취소
            futures.forEach(f -> f.cancel(false));

            log.debug("시간 칭호 체크 취소: sessionId={}", sessionId);
        }
    }

    private void checkAchievement(Long userId, Long sessionId) {
        try {
            List<Achievement> grantedAchievements = achievementService.checkAndGrant(
                    userId,
                    sessionId,
                    TriggerType.TIME_CHECK
            );

            if (!grantedAchievements.isEmpty()) {
                achievementNotifier.notifyNewAchievements(userId, grantedAchievements);

                List<String> achievementCodes = grantedAchievements.stream()
                        .map(Achievement::getCode)
                        .toList();

                log.info("실시간 시간 칭호 달성: userId={}, achievements={}",
                        userId, achievementCodes);
            }
        } catch (Exception e) {
            log.error("Time check failed: sessionId={}", sessionId, e);
        }
    }
}
