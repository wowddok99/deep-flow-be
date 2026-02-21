package com.deepflow.api.integration;

import com.deepflow.api.dto.LogUpdateRequest;
import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import com.deepflow.core.domain.stats.DailyFocusStats;
import com.deepflow.core.domain.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("세션 라이프사이클 통합 테스트")
class SessionLifecycleTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/sessions";
    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = loginAndGetToken("testuser", "password123", "테스트유저");
    }

    @Nested
    @DisplayName("세션 시작")
    class StartSession {

        @Test
        @DisplayName("세션 시작 시 ONGOING 상태로 생성되고 빈 FocusLog가 함께 생성된다")
        void 세션_시작() throws Exception {
            MvcResult result = mockMvc.perform(post(BASE_URL + "/start")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.status").value("ONGOING"))
                    .andExpect(jsonPath("$.data.startTime").isNotEmpty())
                    .andReturn();

            Long sessionId = objectMapper.readTree(
                    result.getResponse().getContentAsString()
            ).path("data").path("id").asLong();

            // FocusLog가 함께 생성되었는지 DB에서 직접 확인 (lazy loading 우회)
            String content = jdbcTemplate.queryForObject(
                    "SELECT fl.content FROM focus_log fl " +
                    "JOIN focus_session fs ON fs.focus_log_id = fl.id WHERE fs.id = ?",
                    String.class, sessionId);
            assertThat(content).isEqualTo("{}");
        }

        @Test
        @DisplayName("이미 진행 중인 세션이 있으면 새 세션을 시작할 수 없다")
        void 중복_세션_시작_방지() throws Exception {
            mockMvc.perform(post(BASE_URL + "/start")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(BASE_URL + "/start")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("SESSION_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("로그 업데이트")
    class UpdateLog {

        @Test
        @DisplayName("세션의 로그를 업데이트하면 제목, 내용, 요약, 이미지가 반영된다")
        void 로그_업데이트() throws Exception {
            Long sessionId = startSessionAndGetId();

            JsonNode tiptapContent = objectMapper.readTree(
                    "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"오늘의 학습 내용\"}]}]}");

            var logRequest = LogUpdateRequest.builder()
                    .title("Spring Security 학습")
                    .content(tiptapContent)
                    .summary("JWT 인증 구현 학습")
                    .imageUrls(List.of("https://img.example.com/1.png", "https://img.example.com/2.png"))
                    .build();

            mockMvc.perform(put(BASE_URL + "/{id}/log", sessionId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logRequest)))
                    .andExpect(status().isOk());

            // 상세 조회로 저장 확인
            mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Spring Security 학습"))
                    .andExpect(jsonPath("$.data.summary").value("JWT 인증 구현 학습"))
                    .andExpect(jsonPath("$.data.content.type").value("doc"))
                    .andExpect(jsonPath("$.data.imageUrls").isArray())
                    .andExpect(jsonPath("$.data.imageUrls.length()").value(2));
        }

        @Test
        @DisplayName("TipTap 형식이 아닌 content를 전송하면 400 반환")
        void 잘못된_content_형식() throws Exception {
            Long sessionId = startSessionAndGetId();

            JsonNode invalidContent = objectMapper.readTree("{\"invalid\":\"format\"}");

            var logRequest = LogUpdateRequest.builder()
                    .content(invalidContent)
                    .build();

            mockMvc.perform(put(BASE_URL + "/{id}/log", sessionId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("세션 종료")
    class StopSession {

        @Test
        @DisplayName("세션 종료 시 COMPLETED 상태로 전환되고 소요시간이 기록된다")
        void 세션_종료() throws Exception {
            Long sessionId = startSessionAndGetId();

            // 약간의 시간차를 두고 종료
            Thread.sleep(100);

            mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            FocusSession session = sessionRepository.findById(sessionId).orElseThrow();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(session.getEndTime()).isNotNull();
            assertThat(session.getDurationSeconds()).isNotNull();
        }

        @Test
        @DisplayName("세션 종료 후 일일 통계가 비동기로 집계된다")
        void 세션_종료시_통계_집계() throws Exception {
            Long sessionId = startSessionAndGetId();

            mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            User user = userRepository.findByUsername("testuser").orElseThrow();

            // 비동기 이벤트 처리 대기
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Optional<DailyFocusStats> stats = dailyFocusStatsRepository
                        .findByUserIdAndDate(user.getId(), LocalDate.now());
                assertThat(stats).isPresent();
                assertThat(stats.get().getTotalSessions()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("여러 세션을 종료하면 통계가 누적된다")
        void 통계_누적() throws Exception {
            // 첫 번째 세션
            Long sessionId1 = startSessionAndGetId();
            mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId1)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            User user = userRepository.findByUsername("testuser").orElseThrow();

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Optional<DailyFocusStats> stats = dailyFocusStatsRepository
                        .findByUserIdAndDate(user.getId(), LocalDate.now());
                assertThat(stats).isPresent();
                assertThat(stats.get().getTotalSessions()).isEqualTo(1);
            });

            // 두 번째 세션
            Long sessionId2 = startSessionAndGetId();
            mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId2)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                DailyFocusStats stats = dailyFocusStatsRepository
                        .findByUserIdAndDate(user.getId(), LocalDate.now()).orElseThrow();
                assertThat(stats.getTotalSessions()).isEqualTo(2);
            });
        }
    }

    @Nested
    @DisplayName("세션 삭제")
    class DeleteSession {

        @Test
        @DisplayName("완료된 세션은 소프트 삭제할 수 있다")
        void 완료된_세션_삭제() throws Exception {
            Long sessionId = startSessionAndGetId();

            // 세션 종료
            mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            // 삭제
            mockMvc.perform(delete(BASE_URL + "/{id}", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent());

            // SQLRestriction에 의해 조회되지 않음
            mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("진행 중인 세션은 삭제할 수 없다")
        void 진행중_세션_삭제_불가() throws Exception {
            Long sessionId = startSessionAndGetId();

            mockMvc.perform(delete(BASE_URL + "/{id}", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("SESSION_NOT_DELETABLE"));
        }
    }

    @Nested
    @DisplayName("세션 목록 조회")
    class GetSessions {

        @Test
        @DisplayName("커서 기반 페이지네이션으로 세션 목록을 조회한다")
        void 커서_페이지네이션() throws Exception {
            // 3개의 완료된 세션 생성
            for (int i = 0; i < 3; i++) {
                Long id = startSessionAndGetId();
                mockMvc.perform(post(BASE_URL + "/{id}/stop", id)
                        .header("Authorization", "Bearer " + accessToken));
            }

            // 첫 페이지 조회 - size=2
            MvcResult firstPage = mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andReturn();

            Long nextCursor = objectMapper.readTree(
                    firstPage.getResponse().getContentAsString()
            ).path("data").path("nextCursorId").asLong();

            // 두 번째 페이지 조회
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("cursorId", nextCursor.toString())
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false));
        }

        @Test
        @DisplayName("다른 유저의 세션은 조회되지 않는다")
        void 유저_격리() throws Exception {
            // 첫 번째 유저가 세션 생성
            startSessionAndGetId();

            // 두 번째 유저 생성 및 로그인
            String otherToken = loginAndGetToken("otheruser", "password123", "다른유저");

            // 두 번째 유저는 세션 목록이 비어있어야 함
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + otherToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("전체 플로우")
    class FullFlow {

        @Test
        @DisplayName("세션 시작 - 로그 작성 - 종료 - 상세 조회 - 통계 확인까지 전체 흐름이 정상 동작한다")
        void 세션_전체_흐름() throws Exception {
            // 1. 세션 시작
            Long sessionId = startSessionAndGetId();

            // 2. 로그 작성
            JsonNode content = objectMapper.readTree(
                    "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"TDD 학습\"}]}]}");

            var logRequest = LogUpdateRequest.builder()
                    .title("TDD 학습 세션")
                    .content(content)
                    .summary("Red-Green-Refactor 패턴 학습")
                    .imageUrls(List.of("https://img.example.com/tdd.png"))
                    .build();

            mockMvc.perform(put(BASE_URL + "/{id}/log", sessionId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logRequest)))
                    .andExpect(status().isOk());

            // 3. 세션 종료
            mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            // 4. 상세 조회 - 모든 데이터가 정합성 있게 반환되는지 확인
            mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.title").value("TDD 학습 세션"))
                    .andExpect(jsonPath("$.data.summary").value("Red-Green-Refactor 패턴 학습"))
                    .andExpect(jsonPath("$.data.content.type").value("doc"))
                    .andExpect(jsonPath("$.data.imageUrls[0]").value("https://img.example.com/tdd.png"))
                    .andExpect(jsonPath("$.data.endTime").isNotEmpty())
                    .andExpect(jsonPath("$.data.durationSeconds").isNumber());

            // 5. 통계 확인
            User user = userRepository.findByUsername("testuser").orElseThrow();

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                DailyFocusStats stats = dailyFocusStatsRepository
                        .findByUserIdAndDate(user.getId(), LocalDate.now()).orElseThrow();
                assertThat(stats.getTotalSessions()).isEqualTo(1);
            });

            // 6. 통계 API 확인
            mockMvc.perform(get("/api/v1/stats/overview")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.todaySessions").value(1))
                    .andExpect(jsonPath("$.data.weekSessions").value(1));
        }
    }

    private Long startSessionAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL + "/start")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).path("data").path("id").asLong();
    }
}
