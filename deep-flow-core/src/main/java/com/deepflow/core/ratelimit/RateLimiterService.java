package com.deepflow.core.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedissonClient redissonClient;

    private static final String VIOLATION_KEY_PREFIX = "rate_limit:violation:";

    public Bucket resolveBucket(String key, boolean isPenalty) {
        CommandAsyncExecutor commandExecutor = ((Redisson) redissonClient).getCommandExecutor();
        
        // Redisson Proxy Manager 생성 (Redis 기반 분산 버킷 관리)
        RedissonBasedProxyManager proxyManager = RedissonBasedProxyManager.builderFor(commandExecutor)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10)))
                .build();

        BucketConfiguration configuration;
        if (isPenalty) {
             // Penalty 모드: 악성 의심 유저는 분당 10회로 제한
            configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build())
                .build();
        } else {
            // 일반 모드: 정상 유저는 분당 100회 허용
            configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillGreedy(100, Duration.ofMinutes(1))
                        .build())
                .build();
        }

        return proxyManager.builder().build(key, configuration);
    }

    public long incrementViolationCount(String key) {
        // 위반 횟수 카운팅 (Redis AtomicLong 사용)
        String violationKey = VIOLATION_KEY_PREFIX + key;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(violationKey);
        long count = atomicLong.incrementAndGet();
        
        // 첫 위반 시 만료 시간 1분 설정
        if (count == 1) {
             atomicLong.expire(Duration.ofMinutes(1)); 
        }
        return count;
    }

    public boolean isInPenaltyBox(String key) {
        // 최근 1분간 위반 50회 초과 시 Penalty 적용
        String violationKey = VIOLATION_KEY_PREFIX + key;
        long count = redissonClient.getAtomicLong(violationKey).get();
        return count > 50;
    }
}
