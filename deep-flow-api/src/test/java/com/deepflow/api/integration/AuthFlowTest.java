package com.deepflow.api.integration;

import com.deepflow.api.dto.LoginRequest;
import com.deepflow.api.dto.SignUpRequest;
import com.deepflow.domain.user.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("인증 플로우 통합 테스트")
class AuthFlowTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/auth";

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("정상 회원가입 시 201 반환, 비밀번호는 BCrypt로 암호화된다")
        void 정상_회원가입() throws Exception {
            var request = new SignUpRequest("testuser", "password123", "테스트유저");

            mockMvc.perform(post(BASE_URL + "/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            User saved = userRepository.findByUsername("testuser").orElseThrow();
            assertThat(saved.getPassword()).isNotEqualTo("password123");
            assertThat(saved.getPassword()).startsWith("$2a$");
            assertThat(saved.getName()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("중복된 username으로 가입하면 409 반환")
        void 중복_username_가입() throws Exception {
            var request = new SignUpRequest("testuser", "password123", "테스트유저");

            // 첫 번째 가입
            mockMvc.perform(post(BASE_URL + "/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // 동일 username 재가입
            mockMvc.perform(post(BASE_URL + "/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("DUPLICATE_USERNAME"));
        }

        @Test
        @DisplayName("username이 6자 미만이면 400 반환")
        void username_길이_제한() throws Exception {
            var request = new SignUpRequest("short", "password123", "테스트유저");

            mockMvc.perform(post(BASE_URL + "/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("정상 로그인 시 access token은 응답 바디에, refresh token은 HttpOnly 쿠키에 담긴다")
        void 정상_로그인() throws Exception {
            signupUser("testuser", "password123", "테스트유저");

            MvcResult result = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("testuser", "password123"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(header().exists("Set-Cookie"))
                    .andReturn();

            String setCookie = result.getResponse().getHeader("Set-Cookie");
            assertThat(setCookie).contains("refreshToken=");
            assertThat(setCookie).contains("HttpOnly");

            // 로그인 후 lastLoginAt이 갱신되었는지 확인
            User user = userRepository.findByUsername("testuser").orElseThrow();
            assertThat(user.getLastLoginAt()).isNotNull();
            assertThat(user.getRefreshToken()).isNotNull();
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 401 반환")
        void 잘못된_비밀번호() throws Exception {
            signupUser("testuser", "password123", "테스트유저");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("testuser", "wrongpassword"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("존재하지 않는 username으로 로그인하면 401 반환")
        void 존재하지_않는_유저() throws Exception {
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("nonexistent", "password123"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        @Test
        @DisplayName("유효한 refresh token으로 재발급 시 새로운 토큰 쌍이 발급된다")
        void 정상_토큰_재발급() throws Exception {
            signupUser("testuser", "password123", "테스트유저");

            // 로그인하여 refresh token 쿠키 획득
            MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("testuser", "password123"))))
                    .andReturn();

            Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
            assertThat(refreshCookie).isNotNull();

            String oldRefreshToken = refreshCookie.getValue();

            // 재발급 요청
            MvcResult reissueResult = mockMvc.perform(post(BASE_URL + "/reissue")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andReturn();

            // 새 refresh token이 이전과 다른지 확인 - rotation 검증
            Cookie newRefreshCookie = reissueResult.getResponse().getCookie("refreshToken");
            assertThat(newRefreshCookie).isNotNull();
            assertThat(newRefreshCookie.getValue()).isNotEqualTo(oldRefreshToken);

            // DB에 새 refresh token이 저장되었는지 확인
            User user = userRepository.findByUsername("testuser").orElseThrow();
            assertThat(user.getRefreshToken()).isEqualTo(newRefreshCookie.getValue());
        }

        @Test
        @DisplayName("이미 교체된 refresh token으로 재발급하면 거부된다")
        void 이미_교체된_토큰으로_재발급_시도() throws Exception {
            signupUser("testuser", "password123", "테스트유저");

            // 로그인
            MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("testuser", "password123"))))
                    .andReturn();

            Cookie originalCookie = loginResult.getResponse().getCookie("refreshToken");
            assertThat(originalCookie).isNotNull();

            // 첫 번째 재발급 - 성공, 기존 토큰은 교체됨
            mockMvc.perform(post(BASE_URL + "/reissue")
                            .cookie(originalCookie))
                    .andExpect(status().isOk());

            // 이미 교체된 토큰으로 재발급 시도 - 탈취 감지 시나리오
            mockMvc.perform(post(BASE_URL + "/reissue")
                            .cookie(originalCookie))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
        }

        @Test
        @DisplayName("refresh token 없이 재발급 요청하면 400 반환")
        void 토큰_없이_재발급_요청() throws Exception {
            mockMvc.perform(post(BASE_URL + "/reissue"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("MISSING_TOKEN"));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("로그아웃 시 DB의 refresh token이 제거되고 쿠키가 만료된다")
        void 정상_로그아웃() throws Exception {
            signupUser("testuser", "password123", "테스트유저");

            MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("testuser", "password123"))))
                    .andReturn();

            Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

            // 로그아웃
            MvcResult logoutResult = mockMvc.perform(post(BASE_URL + "/logout")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andReturn();

            // DB에서 refresh token 제거 확인
            User user = userRepository.findByUsername("testuser").orElseThrow();
            assertThat(user.getRefreshToken()).isNull();

            // Set-Cookie 헤더에서 쿠키 만료 확인
            String setCookie = logoutResult.getResponse().getHeader("Set-Cookie");
            assertThat(setCookie).contains("Max-Age=0");
        }

        @Test
        @DisplayName("로그아웃 후 해당 refresh token으로 재발급이 불가능하다")
        void 로그아웃_후_토큰_재사용_불가() throws Exception {
            signupUser("testuser", "password123", "테스트유저");

            MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("testuser", "password123"))))
                    .andReturn();

            Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

            mockMvc.perform(post(BASE_URL + "/logout").cookie(refreshCookie))
                    .andExpect(status().isOk());

            // 로그아웃 후 동일 토큰으로 재발급 시도
            mockMvc.perform(post(BASE_URL + "/reissue").cookie(refreshCookie))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("JWT 인증 필터")
    class JwtFilter {

        @Test
        @DisplayName("인증이 필요한 엔드포인트에 토큰 없이 접근하면 401 반환")
        void 인증_없이_보호된_엔드포인트_접근() throws Exception {
            mockMvc.perform(post("/api/v1/sessions/start"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효한 JWT로 인증된 요청은 정상 처리된다")
        void 유효한_토큰으로_접근() throws Exception {
            String token = loginAndGetToken("testuser", "password123", "테스트유저");

            mockMvc.perform(post("/api/v1/sessions/start")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isCreated());
        }
    }

    private void signupUser(String username, String password, String name) throws Exception {
        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignUpRequest(username, password, name))))
                .andExpect(status().isCreated());
    }
}
