package com.deepflow.application.port.out.persistence;

import com.deepflow.application.stats.dto.DashboardSummaryStats;
import com.deepflow.domain.stats.DailyFocusStats;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface StatsRepository {

    DailyFocusStats save(DailyFocusStats stats);

    Optional<DailyFocusStats> findByUserIdAndDate(Long userId, LocalDate date);

    List<DailyFocusStats> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    /**
     * 대시보드 개요에 필요한 집계값(전체/이번주/지난주)을 단 한 번의 쿼리로 조회.
     * 기존 4개의 개별 집계 메서드를 대체하여 커넥션 점유를 최소화.
     */
    Optional<DashboardSummaryStats> getDashboardSummary(Long userId,
                                                        LocalDate thisWeekStart,
                                                        LocalDate lastWeekStart,
                                                        LocalDate lastWeekEnd);

    /** @deprecated getDashboardSummary()로 통합. 다른 용도로 사용 중인 경우에만 유지. */
    @Deprecated(since = "refactor/stats-dashboard-optimization")
    int sumSessionsByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    /** @deprecated getDashboardSummary()로 통합. 다른 용도로 사용 중인 경우에만 유지. */
    @Deprecated(since = "refactor/stats-dashboard-optimization")
    long sumDurationByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    /** @deprecated getDashboardSummary()로 통합. 다른 용도로 사용 중인 경우에만 유지. */
    @Deprecated(since = "refactor/stats-dashboard-optimization")
    long sumTotalSessionsByUserId(Long userId);

    /** @deprecated getDashboardSummary()로 통합. 다른 용도로 사용 중인 경우에만 유지. */
    @Deprecated(since = "refactor/stats-dashboard-optimization")
    long sumTotalDurationByUserId(Long userId);


    List<LocalDate> findAllDatesByUserId(Long userId);

    List<DayOfWeekStats> findDayOfWeekStatsByUserId(Long userId);

    // --- Crew activity ---
    List<Long> findUserIdsWithActivityOnDate(List<Long> userIds, LocalDate date);

    long sumDurationByUserIdsOnDate(List<Long> userIds, LocalDate date);

    List<Object[]> findMemberRankingByUserIdsOnDate(List<Long> userIds, LocalDate date, int limit);
}
