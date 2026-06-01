package com.deepflow.infra.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분산 락 내부 실행을 새 트랜잭션으로 감싸는 보조 빈
 *
 * 락 획득 이후 트랜잭션이 시작되도록 DistributedLockAop 에서 별도 빈으로 호출
 */
@Component
public class AopForTransaction {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
