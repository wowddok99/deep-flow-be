package com.deepflow.api.integration;

import com.deepflow.domain.stats.DailyFocusStats;
import com.deepflow.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("통계 API 통합 테스트")
class StatsApiTest extends BaseIntegrationTest {

    private static final String STATS_URL = "/api/v1/stats";
    private String accessToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        accessToken = loginAndGetToken("testuser", "password123", "테스트유저");
        userId = userRepository.findByUsername("testuser").orElseThrow().getId();
    }

    @Test
    @DisplayName("세션 기록이 없으면 모든 통계가 0이다")
    void 빈_통계() throws Exception {
        mockMvc.perform(get(STATS_URL + "/overview")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todaySessions").value(0))
                .andExpect(jsonPath("$.data.todayDurationSeconds").value(0))
                .andExpect(jsonPath("$.data.weekSessions").value(0))
                .andExpect(jsonPath("$.data.weekDurationSeconds").value(0));
    }

    @Test
    @DisplayName("주간 통계 조회 시 최근 7일간의 일별 데이터가 반환된다")
    void 주간_통계() throws Exception {
        LocalDate today = LocalDate.now();

        dailyFocusStatsRepository.save(DailyFocusStats.builder()
                .userId(userId).date(today).totalSessions(3).totalDurationSeconds(5400).build());
        dailyFocusStatsRepository.save(DailyFocusStats.builder()
                .userId(userId).date(today.minusDays(1)).totalSessions(2).totalDurationSeconds(3600).build());
        dailyFocusStatsRepository.save(DailyFocusStats.builder()
                .userId(userId).date(today.minusDays(2)).totalSessions(1).totalDurationSeconds(1800).build());

        mockMvc.perform(get(STATS_URL + "/weekly")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].date").value(today.minusDays(2).toString()))
                .andExpect(jsonPath("$.data[2].date").value(today.toString()));

        mockMvc.perform(get(STATS_URL + "/overview")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todaySessions").value(3))
                .andExpect(jsonPath("$.data.todayDurationSeconds").value(5400))
                .andExpect(jsonPath("$.data.weekSessions").value(6))
                .andExpect(jsonPath("$.data.weekDurationSeconds").value(10800));
    }

    @Test
    @DisplayName("7일 이전의 데이터는 주간 통계에 포함되지 않는다")
    void 주간_범위_초과() throws Exception {
        LocalDate today = LocalDate.now();

        dailyFocusStatsRepository.save(DailyFocusStats.builder()
                .userId(userId).date(today.minusDays(8)).totalSessions(10).totalDurationSeconds(36000).build());

        dailyFocusStatsRepository.save(DailyFocusStats.builder()
                .userId(userId).date(today).totalSessions(1).totalDurationSeconds(1800).build());

        mockMvc.perform(get(STATS_URL + "/overview")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weekSessions").value(1))
                .andExpect(jsonPath("$.data.weekDurationSeconds").value(1800));
    }

    @Test
    @DisplayName("다른 유저의 통계는 조회되지 않는다")
    void 유저_격리() throws Exception {
        String otherToken = loginAndGetToken("otheruser", "password123", "다른유저");
        Long otherUserId = userRepository.findByUsername("otheruser").orElseThrow().getId();

        dailyFocusStatsRepository.save(DailyFocusStats.builder()
                .userId(otherUserId).date(LocalDate.now()).totalSessions(5).totalDurationSeconds(9000).build());

        mockMvc.perform(get(STATS_URL + "/overview")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todaySessions").value(0))
                .andExpect(jsonPath("$.data.weekSessions").value(0));
    }
}
