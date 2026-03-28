package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.port.out.persistence.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 세션 종료 시간대 기반 칭호 평가.
 * 특정 시간대에 세션을 완료한 누적 횟수를 기준으로 판정함.
 */
@Component
@RequiredArgsConstructor
public class TimeZoneEvaluator implements AchievementEvaluator {

    private final SessionRepository sessionRepository;

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        Long userId = context.userId();
        int hour = context.completedSession().getEndTime().getHour();

        // 현재 세션의 종료 시간대에 해당하는 칭호만 체크 (불필요한 DB 조회 방지)
        if (hour >= 5 && hour < 7) {
            long count = sessionRepository.countByUserIdAndEndTimeHourBetween(userId, 5, 7);
            if (!context.alreadyAchieved("T-01") && count >= 3) achieved.add("T-01");   // 얼리버드
            if (!context.alreadyAchieved("T-02") && count >= 10) achieved.add("T-02");  // 모닝 루틴
        }

        if (hour >= 2 && hour < 5) {
            long count = sessionRepository.countByUserIdAndEndTimeHourBetween(userId, 2, 5);
            if (!context.alreadyAchieved("T-03") && count >= 3) achieved.add("T-03");   // 새벽의 수호자
        }

        if (hour >= 0 && hour < 2) {
            long count = sessionRepository.countByUserIdAndEndTimeHourBetween(userId, 0, 2);
            if (!context.alreadyAchieved("T-04") && count >= 3) achieved.add("T-04");   // 밤의 올빼미
            if (!context.alreadyAchieved("T-05") && count >= 10) achieved.add("T-05");  // 올빼미의 둥지
        }

        if (hour >= 12 && hour < 13) {
            long count = sessionRepository.countByUserIdAndEndTimeHourBetween(userId, 12, 13);
            if (!context.alreadyAchieved("T-06") && count >= 5) achieved.add("T-06");   // 점심의 틈새
        }

        if (hour >= 18 && hour < 21) {
            long count = sessionRepository.countByUserIdAndEndTimeHourBetween(userId, 18, 21);
            if (!context.alreadyAchieved("T-07") && count >= 10) achieved.add("T-07");  // 퇴근 후 집중
        }

        return achieved;
    }
}
