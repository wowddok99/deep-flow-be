package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.achievement.UserAchievement;

import java.util.List;
import java.util.Set;

public interface UserAchievementRepository {
    void save(UserAchievement userAchievement);
    List<UserAchievement> findByUserIdWithAchievement(Long userId);
    Set<String> findAchievedCodesByUserId(Long userId);
    long countByUserId(Long userId);
}
