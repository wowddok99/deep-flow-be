package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.port.out.persistence.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 하루 집중도 기반 칭호 평가
 *
 * 당일 세션 횟수와 누적 시간을 기준으로 판정
 */
@Component
@RequiredArgsConstructor
public class DailyIntensityEvaluator implements AchievementEvaluator {

    private final StatsRepository statsRepository;

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        LocalDate today = LocalDate.now();

        var todayStats = statsRepository.findByUserIdAndDate(context.userId(), today).orElse(null);
        if (todayStats == null) return achieved;

        int sessions = todayStats.getTotalSessions();
        long duration = todayStats.getTotalDurationSeconds();

        if (!context.alreadyAchieved("I-01") && sessions >= 2) achieved.add("I-01");
        if (!context.alreadyAchieved("I-02") && sessions >= 3) achieved.add("I-02");
        if (!context.alreadyAchieved("I-03") && sessions >= 5) achieved.add("I-03");

        if (!context.alreadyAchieved("I-04") && duration >= 14_400) achieved.add("I-04");   // 4시간
        if (!context.alreadyAchieved("I-05") && duration >= 28_800) achieved.add("I-05");   // 8시간
        if (!context.alreadyAchieved("I-06") && duration >= 43_200) achieved.add("I-06");   // 12시간

        return achieved;
    }
}
