package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.achievement.TriggerType;
import com.deepflow.domain.session.SessionStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 단일 세션 몰입 시간 기반 칭호 평가
 *
 * 진행 중 세션은 현재 시각까지의 경과 시간으로 실시간 칭호를 판정
 */
@Component
public class DeepDiveEvaluator implements AchievementEvaluator {

    private static final Map<String, Long> THRESHOLDS = new LinkedHashMap<>() {{
        put("D-00", 10L);       // 10초 (스타터)
        put("D-01", 300L);      // 5분
        put("D-02", 900L);      // 15분
        put("D-03", 1_800L);    // 30분
        put("D-04", 3_600L);    // 1시간
        put("D-05", 7_200L);    // 2시간
        put("D-06", 10_800L);   // 3시간
        put("D-07", 14_400L);   // 4시간
        put("D-08", 18_000L);   // 5시간
    }};

    @Override
    public Set<TriggerType> supportedTriggers() {
        return Set.of(TriggerType.SESSION_STOP, TriggerType.TIME_CHECK);
    }

    @Override
    public List<String> evaluate(AchievementContext context) {
        long duration = context.completedSession().getStatus() == SessionStatus.ONGOING
                ? Duration.between(context.completedSession().getStartTime(), LocalDateTime.now()).getSeconds()
                : context.completedSession().getDurationSeconds();

        List<String> achieved = new ArrayList<>();
        for (var entry : THRESHOLDS.entrySet()) {
            if (!context.alreadyAchieved(entry.getKey()) && duration >= entry.getValue()) {
                achieved.add(entry.getKey());
            }
        }
        return achieved;
    }
}
