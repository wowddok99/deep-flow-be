package com.deepflow.application.port.out.notification;

import com.deepflow.domain.achievement.Achievement;

import java.util.List;

/**
 * 칭호 달성 알림을 클라이언트에 전송하는 포트.
 */
public interface AchievementNotifier {

    void notifyNewAchievements(Long userId, List<Achievement> achievements);
}
