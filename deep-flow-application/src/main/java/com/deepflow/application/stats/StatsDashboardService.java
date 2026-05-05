package com.deepflow.application.stats;

import com.deepflow.application.port.out.persistence.*;
import com.deepflow.application.stats.dto.*;
import com.deepflow.domain.stats.DailyFocusStats;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsDashboardService {

    private final StatsRepository statsRepository;
    private final SessionRepository sessionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private Executor executor;

    public DashboardOverviewInfo getDashboardOverview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);

        // 대시보드 요약 및 업적 정보 병렬 조회
        CompletableFuture<DashboardSummaryStats> statsFuture = CompletableFuture.supplyAsync(
                () -> statsRepository.getDashboardSummary(userId, thisWeekStart, lastWeekStart, lastWeekEnd)
                                     .orElse(DashboardSummaryStats.empty()),
                executor);

        CompletableFuture<List<LocalDate>> datesFuture =
                CompletableFuture.supplyAsync(() -> statsRepository.findAllDatesByUserId(userId), executor);

        CompletableFuture<Long> achievementCountFuture =
                CompletableFuture.supplyAsync(() -> userAchievementRepository.countByUserId(userId), executor);

        CompletableFuture<Long> totalAchievementsFuture =
                CompletableFuture.supplyAsync(() -> achievementRepository.count(), executor);

        CompletableFuture.allOf(statsFuture, datesFuture, achievementCountFuture, totalAchievementsFuture).join();

        DashboardSummaryStats stats = statsFuture.join();
        List<LocalDate> dates    = datesFuture.join();
        long achievementCount    = achievementCountFuture.join();
        long totalAchievements   = totalAchievementsFuture.join();

        long avgSession = stats.totalSessions() > 0
                ? stats.totalDurationSeconds() / stats.totalSessions()
                : 0;

        return new DashboardOverviewInfo(
                stats.totalSessions(),
                stats.totalDurationSeconds(),
                avgSession,
                calculateCurrentStreak(dates),
                calculateLongestStreak(dates),
                achievementCount,
                totalAchievements,
                stats.thisWeekSessions(),
                stats.thisWeekDurationSeconds(),
                stats.lastWeekSessions(),
                stats.lastWeekDurationSeconds()
        );
    }

    public List<WeeklyTrendInfo> getWeeklyTrend(Long userId, int weeks) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusWeeks(weeks).with(DayOfWeek.MONDAY);

        List<DailyFocusStats> allStats = statsRepository.findByUserIdAndDateBetween(userId, start, today);

        Map<LocalDate, List<DailyFocusStats>> byWeek = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getDate().with(DayOfWeek.MONDAY)));

        List<WeeklyTrendInfo> result = new ArrayList<>();
        LocalDate weekStart = start;
        while (!weekStart.isAfter(today)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(today)) weekEnd = today;

            List<DailyFocusStats> weekStats = byWeek.getOrDefault(weekStart, List.of());
            int sessions = weekStats.stream().mapToInt(DailyFocusStats::getTotalSessions).sum();
            long duration = weekStats.stream().mapToLong(DailyFocusStats::getTotalDurationSeconds).sum();

            result.add(new WeeklyTrendInfo(weekStart, weekEnd, sessions, duration));
            weekStart = weekStart.plusWeeks(1);
        }
        return result;
    }

    public List<DayOfWeekStatsInfo> getDayOfWeekDistribution(Long userId) {
        List<DayOfWeekStats> raw = statsRepository.findDayOfWeekStatsByUserId(userId);

        Map<Integer, DayOfWeekStats> byDow = raw.stream()
                .collect(Collectors.toMap(DayOfWeekStats::getDayOfWeek, r -> r));

        DayOfWeek[] order = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        List<DayOfWeekStatsInfo> result = new ArrayList<>();
        for (DayOfWeek dow : order) {
            // MySQL DAYOFWEEK(일=1, 월=2...) 인덱스 변환 로직
            int mysqlDow = (dow == DayOfWeek.SUNDAY) ? 1 : dow.getValue() + 1;
            DayOfWeekStats row = byDow.get(mysqlDow);

            long sessions = row != null ? row.getSessions() : 0;
            long duration = row != null ? row.getDuration() : 0;
            result.add(new DayOfWeekStatsInfo(dow.name(), sessions, duration));
        }
        return result;
    }

    @Cacheable(value = "hourlyDistribution", key = "#userId")
    public List<HourlyDistributionInfo> getHourlyDistribution(Long userId) {
        LocalDateTime from = LocalDate.now().minusMonths(6).atStartOfDay();
        Map<Integer, Long> byHour = sessionRepository.findHourlyDistribution(userId, from);
        List<HourlyDistributionInfo> result = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) {
            result.add(new HourlyDistributionInfo(h, byHour.getOrDefault(h, 0L)));
        }
        return result;
    }

    public List<DailyStatsInfo> getCalendarData(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return statsRepository.findByUserIdAndDateBetween(userId, start, end)
                .stream()
                .map(DailyStatsInfo::from)
                .toList();
    }

    public LogActivityInfo getLogActivity(Long userId) {
        return new LogActivityInfo(
                sessionRepository.countLogsWithTitle(userId),
                sessionRepository.countTotalImagesByUserId(userId),
                (int) sessionRepository.avgContentLength(userId)
        );
    }

    private int calculateCurrentStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;

        LocalDate check = LocalDate.now();
        Set<LocalDate> dateSet = new HashSet<>(dates);
        int streak = 0;

        while (dateSet.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }

    private int calculateLongestStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;

        int longest = 1;
        int current = 1;

        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
        }
        return longest;
    }
}
