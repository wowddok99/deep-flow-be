package com.deepflow.infra.persistence.stats;

import com.deepflow.domain.stats.DailyFocusStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface StatsJpaRepository extends JpaRepository<DailyFocusStats, Long> {

    Optional<DailyFocusStats> findByUserIdAndDate(Long userId, LocalDate date);

    List<DailyFocusStats> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(s.totalSessions), 0) FROM DailyFocusStats s WHERE s.userId = :userId AND s.date BETWEEN :from AND :to")
    int sumSessionsByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(s.totalDurationSeconds), 0) FROM DailyFocusStats s WHERE s.userId = :userId AND s.date BETWEEN :from AND :to")
    long sumDurationByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(s.totalSessions), 0) FROM DailyFocusStats s WHERE s.userId = :userId")
    long sumTotalSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(s.totalDurationSeconds), 0) FROM DailyFocusStats s WHERE s.userId = :userId")
    long sumTotalDurationByUserId(@Param("userId") Long userId);

    @Query("SELECT s.date FROM DailyFocusStats s WHERE s.userId = :userId AND s.totalDurationSeconds > 0 ORDER BY s.date ASC")
    List<LocalDate> findAllDatesByUserId(@Param("userId") Long userId);

    @Query("SELECT DAYOFWEEK(s.date), SUM(s.totalSessions), SUM(s.totalDurationSeconds) FROM DailyFocusStats s WHERE s.userId = :userId GROUP BY DAYOFWEEK(s.date)")
    List<Object[]> findDayOfWeekStatsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT s.userId FROM DailyFocusStats s WHERE s.userId IN :userIds AND s.date = :date AND s.totalSessions > 0")
    List<Long> findUserIdsWithActivityOnDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalDurationSeconds), 0) FROM DailyFocusStats s WHERE s.userId IN :userIds AND s.date = :date")
    long sumDurationByUserIdsOnDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date);

    @Query("SELECT s.userId, s.totalDurationSeconds FROM DailyFocusStats s WHERE s.userId IN :userIds AND s.date = :date AND s.totalDurationSeconds > 0 ORDER BY s.totalDurationSeconds DESC")
    List<Object[]> findMemberRankingByUserIdsOnDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date, org.springframework.data.domain.Pageable pageable);
}
