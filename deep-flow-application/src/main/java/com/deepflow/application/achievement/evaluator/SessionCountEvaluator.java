package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 누적 세션 완료 횟수 기반 칭호 평가.
 */
@Component
public class SessionCountEvaluator implements AchievementEvaluator {

    /** 칭호 코드 → 최소 세션 완료 횟수 */
    private static final Map<String, Long> THRESHOLDS = new LinkedHashMap<>() {{
        put("S-01", 5L);
        put("S-02", 10L);
        put("S-03", 30L);
        put("S-04", 50L);
        put("S-05", 100L);
        put("S-06", 200L);
        put("S-07", 500L);
        put("S-08", 1_000L);
    }};

    @Override
    public List<String> evaluate(AchievementContext context) {
        long total = context.totalSessions();
        List<String> achieved = new ArrayList<>();

        for (var entry : THRESHOLDS.entrySet()) {
            if (!context.alreadyAchieved(entry.getKey()) && total >= entry.getValue()) {
                achieved.add(entry.getKey());
            }
        }
        return achieved;
    }
}
