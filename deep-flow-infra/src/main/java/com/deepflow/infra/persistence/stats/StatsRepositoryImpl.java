package com.deepflow.infra.persistence.stats;

import com.deepflow.application.port.out.persistence.StatsRepository;
import com.deepflow.domain.stats.DailyFocusStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {

    private final StatsJpaRepository jpaRepository;

    @Override
    public DailyFocusStats save(DailyFocusStats stats) {
        return jpaRepository.save(stats);
    }

    @Override
    public Optional<DailyFocusStats> findByUserIdAndDate(Long userId, LocalDate date) {
        return jpaRepository.findByUserIdAndDate(userId, date);
    }

    @Override
    public List<DailyFocusStats> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to) {
        return jpaRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
    }

    @Override
    public int sumSessionsByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to) {
        return jpaRepository.sumSessionsByUserIdAndDateBetween(userId, from, to);
    }

    @Override
    public long sumDurationByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to) {
        return jpaRepository.sumDurationByUserIdAndDateBetween(userId, from, to);
    }

    @Override
    public long sumTotalSessionsByUserId(Long userId) {
        return jpaRepository.sumTotalSessionsByUserId(userId);
    }

    @Override
    public long sumTotalDurationByUserId(Long userId) {
        return jpaRepository.sumTotalDurationByUserId(userId);
    }

    @Override
    public List<LocalDate> findAllDatesByUserId(Long userId) {
        return jpaRepository.findAllDatesByUserId(userId);
    }

    @Override
    public List<Object[]> findDayOfWeekStatsByUserId(Long userId) {
        return jpaRepository.findDayOfWeekStatsByUserId(userId);
    }

    @Override
    public List<Long> findUserIdsWithActivityOnDate(List<Long> userIds, LocalDate date) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findUserIdsWithActivityOnDate(userIds, date);
    }

    @Override
    public long sumDurationByUserIdsOnDate(List<Long> userIds, LocalDate date) {
        if (userIds == null || userIds.isEmpty()) {
            return 0L;
        }
        return jpaRepository.sumDurationByUserIdsOnDate(userIds, date);
    }

    @Override
    public List<Object[]> findMemberRankingByUserIdsOnDate(List<Long> userIds, LocalDate date, int limit) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findMemberRankingByUserIdsOnDate(userIds, date, org.springframework.data.domain.PageRequest.of(0, limit));
    }
}
