package com.deepflow.infra.lock;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.lock.LockAcquisitionException;
import com.deepflow.application.lock.DistributedLock;
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

    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(com.deepflow.application.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

        RLock rLock = redissonClient.getLock(key);

        try {
            boolean available = rLock.tryLock(
                    distributedLock.waitTime(), distributedLock.timeUnit());

            if (!available) {
                log.warn("분산락 획득 실패: key={}", key);
                throw new LockAcquisitionException();
            }

            // 락 안에서 트랜잭션을 시작하고 커밋까지 끝내야 락 해제 후 미커밋 상태가 노출되지 않음
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            log.warn("분산락 대기 중 인터럽트: key={}, method={}", key, method.getName(), e);
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException();
        } catch (LockAcquisitionException e) {
            throw e;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("분산락 Redis 예외: key={}, method={}", key, method.getName(), e);
            throw new LockAcquisitionException();
        } finally {
            try {
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.info("분산락 이미 해제됨: method={}, key={}", method.getName(), key);
            }
        }
    }
}
