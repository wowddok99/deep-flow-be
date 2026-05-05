package com.deepflow.infra.persistence.stats;

import com.deepflow.application.port.out.persistence.DayOfWeekStats;
import com.deepflow.application.stats.dto.DashboardSummaryStats;
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

    /**
     * 대시보드 개요 통합 집계 쿼리. COALESCE로 데이터 없을 시 0 반환.
     */
    @Query("SELECT new com.deepflow.application.stats.dto.DashboardSummaryStats(" +
           "COALESCE(SUM(s.totalSessions), 0), " +
           "COALESCE(SUM(s.totalDurationSeconds), 0), " +
           "COALESCE(SUM(CASE WHEN s.date >= :thisWeekStart THEN s.totalSessions ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN s.date >= :thisWeekStart THEN s.totalDurationSeconds ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN s.date >= :lastWeekStart AND s.date <= :lastWeekEnd THEN s.totalSessions ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN s.date >= :lastWeekStart AND s.date <= :lastWeekEnd THEN s.totalDurationSeconds ELSE 0 END), 0)" +
           ") FROM DailyFocusStats s WHERE s.userId = :userId")
    Optional<DashboardSummaryStats> getDashboardSummary(@Param("userId") Long userId,
                                                        @Param("thisWeekStart") LocalDate thisWeekStart,
                                                        @Param("lastWeekStart") LocalDate lastWeekStart,
                                                        @Param("lastWeekEnd") LocalDate lastWeekEnd);

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

    @Query("SELECT DAYOFWEEK(s.date) as dayOfWeek, SUM(s.totalSessions) as sessions, SUM(s.totalDurationSeconds) as duration FROM DailyFocusStats s WHERE s.userId = :userId GROUP BY DAYOFWEEK(s.date)")
    List<DayOfWeekStats> findDayOfWeekStatsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT s.userId FROM DailyFocusStats s WHERE s.userId IN :userIds AND s.date = :date AND s.totalSessions > 0")
    List<Long> findUserIdsWithActivityOnDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalDurationSeconds), 0) FROM DailyFocusStats s WHERE s.userId IN :userIds AND s.date = :date")
    long sumDurationByUserIdsOnDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date);

    @Query("SELECT s.userId, s.totalDurationSeconds FROM DailyFocusStats s WHERE s.userId IN :userIds AND s.date = :date AND s.totalDurationSeconds > 0 ORDER BY s.totalDurationSeconds DESC")
    List<Object[]> findMemberRankingByUserIdsOnDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date, org.springframework.data.domain.Pageable pageable);
}
