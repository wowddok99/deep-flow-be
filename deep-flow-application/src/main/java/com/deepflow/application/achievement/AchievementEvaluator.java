package com.deepflow.application.achievement;

import java.util.List;
import java.util.Set;

public interface AchievementEvaluator {

    List<String> evaluate(AchievementContext context);

    /**
     * 평가기가 반응하는 트리거 타입
     *
     * 기본값은 기존 세션 종료 시점 평가와의 호환성을 위해 SESSION_STOP
     */
    default Set<TriggerType> supportedTriggers() {
        return Set.of(TriggerType.SESSION_STOP);
    }
}
