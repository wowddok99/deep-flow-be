package com.deepflow.application.port.out.notification;

import com.deepflow.domain.achievement.Achievement;

import java.util.List;

public interface AchievementNotifier {

    void notifyNewAchievements(Long userId, List<Achievement> achievements);
}
