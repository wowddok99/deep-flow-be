package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.domain.log.FocusLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 이미지 활용 기반 칭호 평가.
 * 이미지 첨부 로그 수와 총 이미지 수를 기준으로 판정함.
 */
@Component
@RequiredArgsConstructor
public class VisualEvaluator implements AchievementEvaluator {

    private final SessionRepository sessionRepository;

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        Long userId = context.userId();
        FocusLog log = context.completedSession().getFocusLog();

        long logsWithImages = sessionRepository.countByUserIdWithImages(userId);
        long totalImages = sessionRepository.countTotalImagesByUserId(userId);

        if (!context.alreadyAchieved("V-01") && logsWithImages >= 10) achieved.add("V-01");
        if (!context.alreadyAchieved("V-02") && log.getImages() != null && log.getImages().size() >= 3) achieved.add("V-02");
        if (!context.alreadyAchieved("V-03") && logsWithImages >= 50) achieved.add("V-03");
        if (!context.alreadyAchieved("V-04") && totalImages >= 50) achieved.add("V-04");
        if (!context.alreadyAchieved("V-05") && totalImages >= 200) achieved.add("V-05");

        return achieved;
    }
}
