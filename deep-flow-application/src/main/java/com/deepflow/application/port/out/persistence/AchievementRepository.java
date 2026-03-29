package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.achievement.Achievement;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository {
    Optional<Achievement> findByCode(String code);
    List<Achievement> findAll();
    List<Achievement> findByCodes(List<String> codes);
}
