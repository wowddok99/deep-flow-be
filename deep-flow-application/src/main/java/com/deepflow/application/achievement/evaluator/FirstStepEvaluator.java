package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.domain.log.FocusLog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FirstStepEvaluator implements AchievementEvaluator {

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        FocusLog log = context.completedSession().getFocusLog();

        if (!context.alreadyAchieved("F-01")) {
            achieved.add("F-01");
        }

        if (!context.alreadyAchieved("F-02") && log.getTitle() != null && !log.getTitle().isBlank()) {
            achieved.add("F-02");
        }

        if (!context.alreadyAchieved("F-03") && log.getContent() != null && log.getContent().length() >= 100) {
            achieved.add("F-03");
        }

        if (!context.alreadyAchieved("F-04") && log.getImages() != null && !log.getImages().isEmpty()) {
            achieved.add("F-04");
        }

        if (!context.alreadyAchieved("F-05") && log.getSummary() != null && !log.getSummary().isBlank()) {
            achieved.add("F-05");
        }

        return achieved;
    }
}
