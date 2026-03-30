package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.stats.DailyFocusStats;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatsRepository {

    DailyFocusStats save(DailyFocusStats stats);

    Optional<DailyFocusStats> findByUserIdAndDate(Long userId, LocalDate date);

    List<DailyFocusStats> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    int sumSessionsByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    long sumDurationByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    long sumTotalSessionsByUserId(Long userId);

    long sumTotalDurationByUserId(Long userId);

    List<LocalDate> findAllDatesByUserId(Long userId);

    List<Object[]> findDayOfWeekStatsByUserId(Long userId);
}
