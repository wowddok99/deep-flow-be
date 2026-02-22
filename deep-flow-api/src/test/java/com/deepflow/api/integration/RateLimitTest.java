package com.deepflow.api.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Rate Limiting 통합 테스트")
class RateLimitTest extends BaseIntegrationTest {

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = loginAndGetToken("testuser", "password123", "테스트유저");
    }

    @Test
    @DisplayName("정상 요청 시 남은 토큰 수가 헤더에 포함된다")
    void 정상_요청시_남은_토큰_헤더() throws Exception {
        mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Rate-Limit-Ip-Remaining"));
    }

    @Test
    @DisplayName("세션 시작은 일반 요청보다 많은 토큰을 소모한다")
    void 세션_시작_높은_토큰_비용() throws Exception {
        // GET 요청 1회 - cost 1
        String readRemaining = mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("X-Rate-Limit-Ip-Remaining");

        long afterRead = Long.parseLong(readRemaining);

        // POST /sessions/start - cost 10
        String writeRemaining = mockMvc.perform(post("/api/v1/sessions/start")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("X-Rate-Limit-Ip-Remaining");

        long afterWrite = Long.parseLong(writeRemaining);

        // 세션 시작의 토큰 소모량이 단순 조회보다 크다
        assertThat(afterRead - afterWrite).isEqualTo(10);
    }

    @Test
    @DisplayName("IP 기반 토큰이 소진되면 429 반환과 함께 재시도 시간이 안내된다")
    void IP_토큰_소진시_429() throws Exception {
        // 토큰을 빠르게 소진 - refillGreedy에 의한 리필을 고려하여 여유있게 요청
        for (int i = 0; i < 120; i++) {
            mockMvc.perform(get("/api/v1/sessions")
                    .header("Authorization", "Bearer " + accessToken));
        }

        // 토큰 소진 후 요청
        mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-Rate-Limit-Retry-After-Seconds"));
    }

    @Test
    @DisplayName("IP 위반이 50회를 초과하면 패널티 모드가 활성화된다")
    void 패널티_모드_활성화() throws Exception {
        // 토큰을 소진시킨 후 반복 요청으로 위반 횟수 누적 (리필 고려하여 여유있게)
        for (int i = 0; i < 120; i++) {
            mockMvc.perform(get("/api/v1/sessions")
                    .header("Authorization", "Bearer " + accessToken));
        }

        // 50회 이상 위반 누적 (리필 고려하여 여유있게)
        for (int i = 0; i < 60; i++) {
            mockMvc.perform(get("/api/v1/sessions")
                    .header("Authorization", "Bearer " + accessToken));
        }

        // 패널티 모드 확인 - Redis 초기화 후 새 요청에 패널티 헤더가 포함되어야 함
        // 새 버킷이 생성될 때 패널티 모드 적용 여부 확인
        String penalty = mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn().getResponse().getHeader("X-Rate-Limit-Penalty");

        assertThat(penalty).isEqualTo("true");
    }

    @Test
    @DisplayName("인증되지 않은 요청도 IP 기반으로 Rate Limit이 적용된다")
    void 미인증_요청_IP_기반_제한() throws Exception {
        // auth 엔드포인트는 인증 불필요하므로 Rate Limit 테스트 가능
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Rate-Limit-Ip-Remaining"));
    }
}
