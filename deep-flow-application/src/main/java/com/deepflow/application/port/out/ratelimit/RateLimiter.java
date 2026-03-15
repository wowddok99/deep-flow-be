package com.deepflow.application.port.out.ratelimit;

import io.github.bucket4j.Bucket;

public interface RateLimiter {

    Bucket resolveBucket(String key, boolean isPenalty);

    long incrementViolationCount(String key);

    boolean isInPenaltyBox(String key);
}
