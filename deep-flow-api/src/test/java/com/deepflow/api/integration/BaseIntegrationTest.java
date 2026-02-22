package com.deepflow.api.integration;

import com.deepflow.api.service.auth.AuthService;
import com.deepflow.core.repository.log.FocusLogImageRepository;
import com.deepflow.core.repository.session.FocusLogRepository;
import com.deepflow.core.repository.session.FocusSessionRepository;
import com.deepflow.core.repository.stats.DailyFocusStatsRepository;
import com.deepflow.core.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;

import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("deep_flow_test")
            .withUsername("test")
            .withPassword("test");

    static final RedisContainer redis = new RedisContainer("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        mysql.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected FocusSessionRepository sessionRepository;
    @Autowired protected FocusLogRepository focusLogRepository;
    @Autowired protected FocusLogImageRepository focusLogImageRepository;
    @Autowired protected DailyFocusStatsRepository dailyFocusStatsRepository;
    @Autowired protected AuthService authService;
    @Autowired protected RedisTemplate<String, Object> redisTemplate;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        dailyFocusStatsRepository.deleteAllInBatch();
        focusLogImageRepository.deleteAllInBatch();
        // native SQL로 삭제하여 @SQLRestriction("deleted_at IS NULL") 우회
        jdbcTemplate.execute("DELETE FROM focus_session");
        focusLogRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        flushRedis();
    }

    private void flushRedis() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    // 회원가입 후 로그인하여 access token 반환
    protected String loginAndGetToken(String username, String password, String name) {
        authService.signup(new com.deepflow.api.dto.SignUpRequest(username, password, name));
        AuthService.TokenResponse tokens = authService.login(
                new com.deepflow.api.dto.LoginRequest(username, password));
        return tokens.accessToken();
    }
}
