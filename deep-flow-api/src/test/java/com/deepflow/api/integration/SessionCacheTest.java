package com.deepflow.api.integration;

import com.deepflow.api.dto.LogUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("세션 캐시 통합 테스트")
class SessionCacheTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/sessions";
    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = loginAndGetToken("testuser", "password123", "테스트유저");
    }

    @Test
    @DisplayName("세션 상세 조회 시 Redis에 캐시되고, 재조회 시 캐시에서 반환된다")
    void 상세조회_캐시_적중() throws Exception {
        Long sessionId = startSessionAndGetId();

        // 첫 번째 조회 - DB에서 로드, 캐시에 저장
        mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sessionId));

        // Redis에 캐시 키가 존재하는지 확인
        Set<String> keys = redisTemplate.keys("sessions::" + sessionId);
        assertThat(keys).isNotEmpty();

        // 두 번째 조회 - 캐시에서 반환
        mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sessionId));
    }

    @Test
    @DisplayName("로그 업데이트 시 캐시가 무효화되어 변경된 데이터가 반영된다")
    void 로그_업데이트시_캐시_무효화() throws Exception {
        Long sessionId = startSessionAndGetId();

        // 조회하여 캐시 생성
        mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertThat(redisTemplate.keys("sessions::" + sessionId)).isNotEmpty();

        // 로그 업데이트 - 캐시 무효화 발생
        JsonNode content = objectMapper.readTree(
                "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"변경된 내용\"}]}]}");

        var logRequest = LogUpdateRequest.builder()
                .title("업데이트된 제목")
                .content(content)
                .summary("업데이트된 요약")
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}/log", sessionId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isOk());

        // 캐시가 무효화되었는지 확인
        Set<String> keys = redisTemplate.keys("sessions::" + sessionId);
        assertThat(keys).isEmpty();

        // 재조회 시 업데이트된 데이터 반환
        mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("업데이트된 제목"))
                .andExpect(jsonPath("$.data.summary").value("업데이트된 요약"));
    }

    @Test
    @DisplayName("세션 종료 시 캐시가 무효화된다")
    void 세션_종료시_캐시_무효화() throws Exception {
        Long sessionId = startSessionAndGetId();

        // 캐시 생성
        mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertThat(redisTemplate.keys("sessions::" + sessionId)).isNotEmpty();

        // 세션 종료 - 캐시 무효화
        mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertThat(redisTemplate.keys("sessions::" + sessionId)).isEmpty();

        // 재조회 시 COMPLETED 상태 반환
        mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("JsonNode 타입의 content가 Redis 직렬화 후 역직렬화되어도 구조가 유지된다")
    void 캐시_JsonNode_직렬화_정합성() throws Exception {
        Long sessionId = startSessionAndGetId();

        // 복잡한 TipTap 구조의 content 설정
        String complexJson = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "heading",
                      "attrs": {"level": 1},
                      "content": [{"type": "text", "text": "제목"}]
                    },
                    {
                      "type": "paragraph",
                      "content": [
                        {"type": "text", "text": "일반 텍스트 "},
                        {"type": "text", "marks": [{"type": "bold"}], "text": "굵은 텍스트"}
                      ]
                    },
                    {
                      "type": "bulletList",
                      "content": [
                        {"type": "listItem", "content": [{"type": "paragraph", "content": [{"type": "text", "text": "항목 1"}]}]},
                        {"type": "listItem", "content": [{"type": "paragraph", "content": [{"type": "text", "text": "항목 2"}]}]}
                      ]
                    }
                  ]
                }
                """;

        JsonNode content = objectMapper.readTree(complexJson);

        var logRequest = LogUpdateRequest.builder()
                .title("직렬화 테스트")
                .content(content)
                .summary("복잡한 TipTap 구조")
                .imageUrls(List.of("https://img.example.com/test.png"))
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}/log", sessionId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isOk());

        // 첫 조회 - DB에서 로드하여 캐시에 저장
        MvcResult firstResult = mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // 두 번째 조회 - 캐시에서 역직렬화
        MvcResult cachedResult = mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // 캐시 전후로 content 구조가 동일한지 검증
        JsonNode firstContent = objectMapper.readTree(firstResult.getResponse().getContentAsString())
                .path("data").path("content");
        JsonNode cachedContent = objectMapper.readTree(cachedResult.getResponse().getContentAsString())
                .path("data").path("content");

        assertThat(cachedContent).isEqualTo(firstContent);
        assertThat(cachedContent.path("content").get(0).path("type").asText()).isEqualTo("heading");
        assertThat(cachedContent.path("content").get(2).path("type").asText()).isEqualTo("bulletList");
    }

    @Test
    @DisplayName("LocalDateTime 필드가 Redis 직렬화 후 역직렬화되어도 정확히 복원된다")
    void 캐시_LocalDateTime_직렬화_정합성() throws Exception {
        Long sessionId = startSessionAndGetId();

        // 세션 종료하여 endTime 생성
        mockMvc.perform(post(BASE_URL + "/{id}/stop", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 첫 조회 - 캐시 저장
        MvcResult firstResult = mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // 두 번째 조회 - 캐시에서 복원
        MvcResult cachedResult = mockMvc.perform(get(BASE_URL + "/{id}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstData = objectMapper.readTree(firstResult.getResponse().getContentAsString()).path("data");
        JsonNode cachedData = objectMapper.readTree(cachedResult.getResponse().getContentAsString()).path("data");

        // startTime, endTime이 캐시 전후로 동일
        assertThat(cachedData.path("startTime").asText())
                .isEqualTo(firstData.path("startTime").asText());
        assertThat(cachedData.path("endTime").asText())
                .isEqualTo(firstData.path("endTime").asText());
        assertThat(cachedData.path("durationSeconds").asLong())
                .isEqualTo(firstData.path("durationSeconds").asLong());
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
