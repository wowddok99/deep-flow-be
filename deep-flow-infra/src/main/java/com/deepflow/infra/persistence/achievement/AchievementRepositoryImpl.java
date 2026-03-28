package com.deepflow.infra.persistence.achievement;

import com.deepflow.application.port.out.persistence.AchievementRepository;
import com.deepflow.domain.achievement.Achievement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AchievementRepositoryImpl implements AchievementRepository {

    private final AchievementJpaRepository jpaRepository;

    @Override
    public Optional<Achievement> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<Achievement> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Achievement> findByCodes(List<String> codes) {
        return jpaRepository.findByCodeIn(codes);
    }
}
