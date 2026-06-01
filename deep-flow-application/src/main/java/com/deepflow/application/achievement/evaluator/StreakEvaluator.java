package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 연속 기록 일수 기반 칭호 평가
 */
@Component
public class StreakEvaluator implements AchievementEvaluator {

    private static final Map<String, Integer> THRESHOLDS = new LinkedHashMap<>() {{
        put("K-01", 2);
        put("K-02", 3);
        put("K-03", 7);
        put("K-04", 14);
        put("K-05", 21);
        put("K-06", 30);
        put("K-07", 50);
        put("K-08", 100);
        put("K-09", 365);
    }};

    @Override
    public List<String> evaluate(AchievementContext context) {
        int streak = context.currentStreak();
        List<String> achieved = new ArrayList<>();

        for (var entry : THRESHOLDS.entrySet()) {
            if (!context.alreadyAchieved(entry.getKey()) && streak >= entry.getValue()) {
                achieved.add(entry.getKey());
            }
        }
        return achieved;
    }
}
