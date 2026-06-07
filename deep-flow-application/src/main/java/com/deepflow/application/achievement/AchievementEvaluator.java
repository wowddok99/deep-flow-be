package com.deepflow.application.achievement;

import java.util.List;
import java.util.Set;

public interface AchievementEvaluator {

    /**
     * 전달된 컨텍스트를 기준으로 달성 가능한 칭호 코드를 반환
     */
    List<String> evaluate(AchievementContext context);

    /**
     * 해당 칭호 조건을 확인할 트리거 목록
     *
     * 별도 지정이 없으면 세션 종료 시점에만 확인
     */
    default Set<TriggerType> supportedTriggers() {
        return Set.of(TriggerType.SESSION_STOP);
    }
}
