package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 단일 세션 몰입 시간 기반 칭호 평가.
 * 한 번의 세션에서 연속으로 집중한 시간이 기준값을 넘으면 칭호를 부여함.
 */
@Component
public class DeepDiveEvaluator implements AchievementEvaluator {

    /** 칭호 코드 → 최소 세션 시간(초) */
    private static final Map<String, Long> THRESHOLDS = new LinkedHashMap<>() {{
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
    public List<String> evaluate(AchievementContext context) {
        long duration = context.completedSession().getDurationSeconds();
        List<String> achieved = new ArrayList<>();

        for (var entry : THRESHOLDS.entrySet()) {
            if (!context.alreadyAchieved(entry.getKey()) && duration >= entry.getValue()) {
                achieved.add(entry.getKey());
            }
        }
        return achieved;
    }
}
