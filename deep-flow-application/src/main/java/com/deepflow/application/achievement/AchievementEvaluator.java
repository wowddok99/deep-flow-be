package com.deepflow.application.achievement;

import java.util.List;

public interface AchievementEvaluator {
    List<String> evaluate(AchievementContext context);
}
