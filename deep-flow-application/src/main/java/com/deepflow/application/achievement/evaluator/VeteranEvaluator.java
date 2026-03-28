package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class VeteranEvaluator implements AchievementEvaluator {

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        long daysSinceJoin = ChronoUnit.DAYS.between(context.userCreatedDate(), LocalDate.now());
        long totalSessions = context.totalSessions();

        // 가입 경과일 + 최소 세션 수 동시 충족 필요
        if (!context.alreadyAchieved("E-01") && daysSinceJoin >= 7 && totalSessions >= 1) achieved.add("E-01");     // 7일 + 1회
        if (!context.alreadyAchieved("E-02") && daysSinceJoin >= 30 && totalSessions >= 5) achieved.add("E-02");    // 30일 + 5회
        if (!context.alreadyAchieved("E-03") && daysSinceJoin >= 90 && totalSessions >= 20) achieved.add("E-03");   // 90일 + 20회
        if (!context.alreadyAchieved("E-04") && daysSinceJoin >= 180 && totalSessions >= 50) achieved.add("E-04");  // 180일 + 50회
        if (!context.alreadyAchieved("E-05") && daysSinceJoin >= 365 && totalSessions >= 100) achieved.add("E-05"); // 365일 + 100회

        return achieved;
    }
}
