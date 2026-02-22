package com.deepflow.api.integration;

import com.deepflow.domain.session.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("세션 동시성 통합 테스트")
class SessionConcurrencyTest extends BaseIntegrationTest {

    private static final String START_URL = "/api/v1/sessions/start";
    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = loginAndGetToken("testuser", "password123", "테스트유저");
    }

    @Test
    @DisplayName("동시에 세션을 시작하면 분산 락에 의해 하나만 생성된다")
    void 동시_세션_시작시_하나만_생성() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                latch.await();
                MvcResult result = mockMvc.perform(post(START_URL)
                                .header("Authorization", "Bearer " + accessToken))
                        .andReturn();
                return result.getResponse().getStatus();
            }));
        }

        // 모든 스레드가 동시에 시작하도록 카운트다운
        latch.countDown();

        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statuses.add(future.get());
        }

        executor.shutdown();

        long successCount = statuses.stream().filter(s -> s == 201).count();
        long conflictCount = statuses.stream().filter(s -> s == 409).count();

        // 정확히 하나만 성공
        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(threadCount - 1);

        // DB에도 ONGOING 세션이 하나만 존재
        long ongoingCount = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SessionStatus.ONGOING)
                .count();
        assertThat(ongoingCount).isEqualTo(1);
    }

    @Test
    @DisplayName("서로 다른 유저가 동시에 세션을 시작하면 각각 성공한다")
    void 다른_유저_동시_세션_시작() throws Exception {
        String tokenA = accessToken;
        String tokenB = loginAndGetToken("userB00", "password123", "유저B");

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Integer> futureA = executor.submit(() -> {
            latch.await();
            return mockMvc.perform(post(START_URL)
                            .header("Authorization", "Bearer " + tokenA))
                    .andReturn().getResponse().getStatus();
        });

        Future<Integer> futureB = executor.submit(() -> {
            latch.await();
            return mockMvc.perform(post(START_URL)
                            .header("Authorization", "Bearer " + tokenB))
                    .andReturn().getResponse().getStatus();
        });

        latch.countDown();

        assertThat(futureA.get()).isEqualTo(201);
        assertThat(futureB.get()).isEqualTo(201);

        executor.shutdown();

        // 각 유저마다 ONGOING 세션이 하나씩 존재
        long totalOngoing = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SessionStatus.ONGOING)
                .count();
        assertThat(totalOngoing).isEqualTo(2);
    }
}
