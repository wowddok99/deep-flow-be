package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.port.out.persistence.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 기록 깊이(로그 작성) 기반 칭호 평가.
 * 로그 본문 길이별 작성 횟수를 기준으로 판정함.
 */
@Component
@RequiredArgsConstructor
public class WriterEvaluator implements AchievementEvaluator {

    private final SessionRepository sessionRepository;

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        Long userId = context.userId();

        long contentLogs100 = sessionRepository.countByUserIdWithMinContentLength(userId, 100);
        long contentLogs500 = sessionRepository.countByUserIdWithMinContentLength(userId, 500);
        long contentLogs1000 = sessionRepository.countByUserIdWithMinContentLength(userId, 1000);
        long contentLogs2000 = sessionRepository.countByUserIdWithMinContentLength(userId, 2000);

        // 본문 품질 기반 (긴 글 N회 작성)
        if (!context.alreadyAchieved("W-03") && contentLogs500 >= 5) achieved.add("W-03");    // 500자 이상 5개
        if (!context.alreadyAchieved("W-04") && contentLogs1000 >= 10) achieved.add("W-04");  // 1,000자 이상 10개
        if (!context.alreadyAchieved("W-05") && contentLogs2000 >= 5) achieved.add("W-05");   // 2,000자 이상 5개

        // 본문 양 기반 (100자 이상 로그 누적)
        if (!context.alreadyAchieved("W-06") && contentLogs100 >= 50) achieved.add("W-06");
        if (!context.alreadyAchieved("W-07") && contentLogs100 >= 200) achieved.add("W-07");
        if (!context.alreadyAchieved("W-08") && contentLogs100 >= 500) achieved.add("W-08");

        return achieved;
    }
}
