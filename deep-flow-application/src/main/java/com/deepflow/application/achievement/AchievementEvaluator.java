package com.deepflow.application.achievement;

import java.util.List;
import java.util.Set;

public interface AchievementEvaluator {

    List<String> evaluate(AchievementContext context);

    /**
     * 이 Evaluator가 반응하는 트리거 타입.
     * 기본값은 SESSION_STOP만 (기존 동작 유지).
     */
    default Set<TriggerType> supportedTriggers() {
        return Set.of(TriggerType.SESSION_STOP);
    }
}
