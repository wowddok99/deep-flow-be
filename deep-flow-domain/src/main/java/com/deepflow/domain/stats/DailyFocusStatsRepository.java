package com.deepflow.domain.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyFocusStatsRepository extends JpaRepository<DailyFocusStats, Long> {
    Optional<DailyFocusStats> findByUserIdAndDate(Long userId, LocalDate date);

    List<DailyFocusStats> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(s.totalSessions), 0) FROM DailyFocusStats s WHERE s.userId = :userId AND s.date BETWEEN :from AND :to")
    int sumSessionsByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(s.totalDurationSeconds), 0) FROM DailyFocusStats s WHERE s.userId = :userId AND s.date BETWEEN :from AND :to")
    long sumDurationByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
