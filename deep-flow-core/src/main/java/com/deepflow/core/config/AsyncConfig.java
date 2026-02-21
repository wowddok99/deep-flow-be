package com.deepflow.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // 평상시 유지되는 스레드
        executor.setMaxPoolSize(30);       // 큐 포화 시 확장 가능한 최대 스레드
        executor.setQueueCapacity(50);     // 코어 스레드 초과 요청 대기열
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
