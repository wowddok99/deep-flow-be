package com.deepflow.application.stats;

import com.deepflow.application.port.out.persistence.*;
import com.deepflow.application.stats.dto.*;
import com.deepflow.domain.stats.DailyFocusStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepflow.domain.session.FocusSession;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsDashboardService {

    private final StatsRepository statsRepository;
    private final SessionRepository sessionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;

    public DashboardOverviewInfo getDashboardOverview(Long userId) {
        long totalSessions = statsRepository.sumTotalSessionsByUserId(userId);
        long totalDuration = statsRepository.sumTotalDurationByUserId(userId);
        long avgSession = totalSessions > 0 ? totalDuration / totalSessions : 0;

        List<LocalDate> dates = statsRepository.findAllDatesByUserId(userId);
        int currentStreak = calculateCurrentStreak(dates);
        int longestStreak = calculateLongestStreak(dates);

        long achievementCount = userAchievementRepository.countByUserId(userId);
        long totalAchievements = achievementRepository.findAll().size();

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);

        long thisWeekSessions = statsRepository.sumSessionsByUserIdAndDateBetween(userId, thisWeekStart, today);
        long thisWeekDuration = statsRepository.sumDurationByUserIdAndDateBetween(userId, thisWeekStart, today);
        long lastWeekSessions = statsRepository.sumSessionsByUserIdAndDateBetween(userId, lastWeekStart, lastWeekEnd);
        long lastWeekDuration = statsRepository.sumDurationByUserIdAndDateBetween(userId, lastWeekStart, lastWeekEnd);

        return new DashboardOverviewInfo(
                totalSessions, totalDuration, avgSession,
                currentStreak, longestStreak,
                achievementCount, totalAchievements,
                thisWeekSessions, thisWeekDuration,
                lastWeekSessions, lastWeekDuration
        );
    }

    public List<WeeklyTrendInfo> getWeeklyTrend(Long userId, int weeks) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusWeeks(weeks).with(DayOfWeek.MONDAY);

        List<DailyFocusStats> allStats = statsRepository.findByUserIdAndDateBetween(userId, start, today);

        // 날짜별 stats를 주 단위로 그룹핑
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
        List<Object[]> raw = statsRepository.findDayOfWeekStatsByUserId(userId);

        // MySQL DAYOFWEEK: 1=일, 2=월, ..., 7=토
        String[] dayNames = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};

        Map<Integer, Object[]> byDow = raw.stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).intValue(), r -> r));

        List<DayOfWeekStatsInfo> result = new ArrayList<>();
        // 월요일부터 시작
        int[] order = {2, 3, 4, 5, 6, 7, 1};
        for (int dow : order) {
            Object[] row = byDow.get(dow);
            long sessions = row != null ? ((Number) row[1]).longValue() : 0;
            long duration = row != null ? ((Number) row[2]).longValue() : 0;
            result.add(new DayOfWeekStatsInfo(dayNames[dow - 1], sessions, duration));
        }
        return result;
    }

    @Cacheable(value = "hourlyDistribution", key = "#userId")
    public List<HourlyDistributionInfo> getHourlyDistribution(Long userId) {
        LocalDateTime from = LocalDate.now().minusMonths(6).atStartOfDay();
        List<FocusSession> sessions = sessionRepository.findCompletedSessionsAfter(userId, from);
        long[] counts = new long[24];

        for (FocusSession s : sessions) {
            LocalDateTime cursor = s.getStartTime().truncatedTo(ChronoUnit.HOURS);
            LocalDateTime endTruncated = s.getEndTime().truncatedTo(ChronoUnit.HOURS);

            if (s.getEndTime().equals(endTruncated)) {
                endTruncated = endTruncated.minusHours(1);
            }

            while (!cursor.isAfter(endTruncated)) {
                counts[cursor.getHour()]++;
                cursor = cursor.plusHours(1);
            }
        }

        List<HourlyDistributionInfo> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            result.add(new HourlyDistributionInfo(h, counts[h]));
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
        long totalLogs = sessionRepository.countLogsWithTitle(userId);
        long totalImages = sessionRepository.countTotalImagesByUserId(userId);
        int avgLength = (int) sessionRepository.avgContentLength(userId);

        return new LogActivityInfo(totalLogs, totalImages, avgLength);
    }

    private int calculateCurrentStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate check = today;

        Set<LocalDate> dateSet = new HashSet<>(dates);
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
