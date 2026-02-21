package com.deepflow.core.aop;

import com.deepflow.core.annotation.DistributedLock;
import com.deepflow.core.utils.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    // Redis Lock 키 prefix
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    /**
     * @DistributedLock이 붙은 메서드 실행 시
     * Redis 분산락을 획득하여
     * 동일 작업의 동시 실행을 막는 AOP
     */
    @Around("@annotation(com.deepflow.core.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 어노테이션 정보 조회
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // SpEL 기반 동적 키 생성
        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

        // Redis 분산락 객체 획득
        RLock rLock = redissonClient.getLock(key);

        try {
            // 설정된 waitTime/leaseTime 기준으로 락 시도
            boolean available = rLock.tryLock(
                    distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());

            // 락 획득 실패 시 예외
            if (!available) {
                throw new IllegalStateException("Failed to acquire lock: " + key);
            }

            // 락 획득 성공 → 별도 트랜잭션에서 비즈니스 로직 수행
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            // 락 해제 (leaseTime 만료 등으로 이미 해제된 경우 예외 무시)
            try {
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already Unlocked: {} {}", method.getName(), key);
            }
        }
    }
}
