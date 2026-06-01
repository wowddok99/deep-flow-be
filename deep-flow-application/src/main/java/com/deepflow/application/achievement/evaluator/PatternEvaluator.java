package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.StatsRepository;
import com.deepflow.domain.stats.DailyFocusStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 요일과 주 단위 기록 패턴 기반 칭호 평가
 */
@Component
@RequiredArgsConstructor
public class PatternEvaluator implements AchievementEvaluator {

    private final SessionRepository sessionRepository;
    private final StatsRepository statsRepository;

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        Long userId = context.userId();

        if (!context.alreadyAchieved("P-01")) {
            long count = sessionRepository.countByUserIdAndDayOfWeek(userId, 1); // 월요일
            if (count >= 10) achieved.add("P-01");
        }

        if (!context.alreadyAchieved("P-02")) {
            long count = sessionRepository.countByUserIdAndDayOfWeek(userId, 5); // 금요일
            if (count >= 10) achieved.add("P-02");
        }

        if (!context.alreadyAchieved("P-03")) {
            long satCount = sessionRepository.countByUserIdAndDayOfWeek(userId, 6);
            long sunCount = sessionRepository.countByUserIdAndDayOfWeek(userId, 7);
            if ((satCount + sunCount) >= 10) achieved.add("P-03");
        }

        // 풀위크 계산은 1년치 데이터를 한 번에 조회해 반복 쿼리 방지
        if (!context.alreadyAchieved("P-04") || !context.alreadyAchieved("P-05")) {
            long fullWeeks = countFullWeeks(userId);
            if (!context.alreadyAchieved("P-04") && fullWeeks >= 1) achieved.add("P-04");
            if (!context.alreadyAchieved("P-05") && fullWeeks >= 4) achieved.add("P-05");
        }

        return achieved;
    }

    private long countFullWeeks(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate yearAgo = today.minusWeeks(52).with(DayOfWeek.MONDAY);

        Set<LocalDate> activeDates = statsRepository.findByUserIdAndDateBetween(userId, yearAgo, today)
                .stream()
                .map(DailyFocusStats::getDate)
                .collect(Collectors.toSet());

        long count = 0;
        for (int w = 0; w < 52; w++) {
            LocalDate weekStart = today.minusWeeks(w).with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(today)) continue;

            boolean allDays = true;
            for (int d = 0; d < 7; d++) {
                if (!activeDates.contains(weekStart.plusDays(d))) {
                    allDays = false;
                    break;
                }
            }
            if (allDays) count++;
        }
        return count;
    }
}
