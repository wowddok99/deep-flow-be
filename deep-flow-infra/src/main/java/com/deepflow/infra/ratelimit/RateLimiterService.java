package com.deepflow.infra.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedissonClient redissonClient;

    private static final String VIOLATION_KEY_PREFIX = "rate_limit:violation:";
    private static final int NORMAL_RATE_LIMIT = 100;
    private static final int PENALTY_RATE_LIMIT = 10;
    private static final long PENALTY_THRESHOLD = 50;

    public Bucket resolveBucket(String key, boolean isPenalty) {
        CommandAsyncExecutor commandExecutor = ((Redisson) redissonClient).getCommandExecutor();

        RedissonBasedProxyManager proxyManager = RedissonBasedProxyManager.builderFor(commandExecutor)
                .withExpirationStrategy(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10)))
                .build();

        int limit = isPenalty ? PENALTY_RATE_LIMIT : NORMAL_RATE_LIMIT;
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, Duration.ofMinutes(1))
                        .build())
                .build();

        return proxyManager.builder().build(key, configuration);
    }

    public long incrementViolationCount(String key) {
        String violationKey = VIOLATION_KEY_PREFIX + key;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(violationKey);
        long count = atomicLong.incrementAndGet();

        if (count == 1) {
            atomicLong.expire(Duration.ofMinutes(1));
        }

        if (count == PENALTY_THRESHOLD + 1) {
            log.warn("페널티 박스 진입: key={}, violations={}", key, count);
        }

        return count;
    }

    public boolean isInPenaltyBox(String key) {
        String violationKey = VIOLATION_KEY_PREFIX + key;
        long count = redissonClient.getAtomicLong(violationKey).get();
        return count > PENALTY_THRESHOLD;
    }
}
