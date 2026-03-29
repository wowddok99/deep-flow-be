package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.achievement.TriggerType;
import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.SessionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 첫 걸음 관련 칭호 평가 (첫 세션 완료, 첫 제목/본문/이미지/요약 작성) */
@Component
public class FirstStepEvaluator implements AchievementEvaluator {

    @Override
    public Set<TriggerType> supportedTriggers() {
        return Set.of(TriggerType.SESSION_STOP, TriggerType.LOG_UPDATE);
    }

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        FocusLog log = context.completedSession().getFocusLog();

        // F-01: 세션 종료 시에만 (첫 세션 완료)
        if (!context.alreadyAchieved("F-01")
                && context.completedSession().getStatus() == SessionStatus.COMPLETED) {
            achieved.add("F-01");
        }

        // F-02~F-05: 로그 업데이트 시에도 즉시 감지
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
