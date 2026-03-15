package com.deepflow.infra.ratelimit;

import com.deepflow.application.port.out.ratelimit.RateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RateLimiterService implements RateLimiter {

    private final RedissonBasedProxyManager proxyManager;
    private final RedissonClient redissonClient;

    private static final String VIOLATION_KEY_PREFIX = "rate_limit:violation:";

    @Value("${app.rate-limit.normal-rate-limit:100}")
    private int normalRateLimit;

    @Value("${app.rate-limit.penalty-rate-limit:10}")
    private int penaltyRateLimit;

    @Value("${app.rate-limit.penalty-threshold:50}")
    private long penaltyThreshold;

    @Value("${app.rate-limit.refill-period-minutes:1}")
    private long refillPeriodMinutes;

    @Value("${app.rate-limit.violation-expiry-minutes:1}")
    private long violationExpiryMinutes;

    public RateLimiterService(
            RedissonClient redissonClient,
            @Value("${app.rate-limit.bucket-expiry-minutes:10}") long bucketExpiryMinutes) {
        this.redissonClient = redissonClient;

        CommandAsyncExecutor commandExecutor = ((Redisson) redissonClient).getCommandExecutor();
        this.proxyManager = RedissonBasedProxyManager.builderFor(commandExecutor)
                .withExpirationStrategy(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(bucketExpiryMinutes)))
                .build();
    }

    public Bucket resolveBucket(String key, boolean isPenalty) {
        int limit = isPenalty ? penaltyRateLimit : normalRateLimit;
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, Duration.ofMinutes(refillPeriodMinutes))
                        .build())
                .build();

        return proxyManager.builder().build(key, configuration);
    }

    public long incrementViolationCount(String key) {
        String violationKey = VIOLATION_KEY_PREFIX + key;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(violationKey);
        long count = atomicLong.incrementAndGet();

        if (count == 1) {
            atomicLong.expire(Duration.ofMinutes(violationExpiryMinutes));
        }

        if (count == penaltyThreshold + 1) {
            log.warn("페널티 박스 진입: key={}, violations={}", key, count);
        }

        return count;
    }

    public boolean isInPenaltyBox(String key) {
        String violationKey = VIOLATION_KEY_PREFIX + key;
        long count = redissonClient.getAtomicLong(violationKey).get();
        return count > penaltyThreshold;
    }
}
