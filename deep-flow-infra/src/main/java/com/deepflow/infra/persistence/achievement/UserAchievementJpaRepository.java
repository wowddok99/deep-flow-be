package com.deepflow.infra.persistence.achievement;

import com.deepflow.domain.achievement.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

interface UserAchievementJpaRepository extends JpaRepository<UserAchievement, Long> {

    @Query("SELECT ua FROM UserAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId ORDER BY ua.achievedAt DESC")
    List<UserAchievement> findByUserIdWithAchievement(@Param("userId") Long userId);

    @Query("SELECT ua.achievement.code FROM UserAchievement ua WHERE ua.user.id = :userId")
    Set<String> findAchievedCodesByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);
}
