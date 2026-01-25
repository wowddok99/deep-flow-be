package com.deepflow.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락의 이름 (고유 키)
     * SpEL을 사용하여 파라미터 값을 조합할 수 있습니다. 예: "session:#userId"
     */
    String key();

    /**
     * 락의 시간 단위 (기본: 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 락 획득을 위해 대기할 최대 시간 (기본: 5초)
     * 이 시간이 지나면 락 획득 실패로 간주합니다.
     */
    long waitTime() default 5L;

    /**
     * 락을 획득한 후 점유할 최대 시간 (기본: 3초)
     * 이 시간이 지나면 락이 자동으로 해제됩니다 (Deadlock 방지).
     */
    long leaseTime() default 3L;
}
