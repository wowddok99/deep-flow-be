package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 누적 몰입 시간 기반 칭호 평가.
 * 유저의 전체 세션 시간 합계가 기준값을 넘으면 칭호를 부여함.
 */
@Component
public class GrowthRingEvaluator implements AchievementEvaluator {

    /** 칭호 코드 → 최소 누적 시간(초) */
    private static final Map<String, Long> THRESHOLDS = new LinkedHashMap<>() {{
        put("G-01", 3_600L);       // 1시간
        put("G-02", 18_000L);      // 5시간
        put("G-03", 36_000L);      // 10시간
        put("G-04", 72_000L);      // 20시간
        put("G-05", 180_000L);     // 50시간
        put("G-06", 360_000L);     // 100시간
        put("G-07", 720_000L);     // 200시간
        put("G-08", 1_800_000L);   // 500시간
        put("G-09", 3_600_000L);   // 1,000시간
    }};

    @Override
    public List<String> evaluate(AchievementContext context) {
        long total = context.totalDurationSeconds();
        List<String> achieved = new ArrayList<>();

        for (var entry : THRESHOLDS.entrySet()) {
            if (!context.alreadyAchieved(entry.getKey()) && total >= entry.getValue()) {
                achieved.add(entry.getKey());
            }
        }
        return achieved;
    }
}
