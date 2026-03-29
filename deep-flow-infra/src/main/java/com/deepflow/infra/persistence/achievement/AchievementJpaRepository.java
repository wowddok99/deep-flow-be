package com.deepflow.infra.persistence.achievement;

import com.deepflow.domain.achievement.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AchievementJpaRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByCode(String code);
    List<Achievement> findByCodeIn(List<String> codes);
}
