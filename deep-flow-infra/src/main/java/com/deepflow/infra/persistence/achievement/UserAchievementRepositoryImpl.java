package com.deepflow.infra.persistence.achievement;

import com.deepflow.application.port.out.persistence.UserAchievementRepository;
import com.deepflow.domain.achievement.UserAchievement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserAchievementRepositoryImpl implements UserAchievementRepository {

    private final UserAchievementJpaRepository jpaRepository;

    @Override
    public void save(UserAchievement userAchievement) {
        jpaRepository.save(userAchievement);
    }

    @Override
    public List<UserAchievement> findByUserIdWithAchievement(Long userId) {
        return jpaRepository.findByUserIdWithAchievement(userId);
    }

    @Override
    public Set<String> findAchievedCodesByUserId(Long userId) {
        return jpaRepository.findAchievedCodesByUserId(userId);
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }
}
