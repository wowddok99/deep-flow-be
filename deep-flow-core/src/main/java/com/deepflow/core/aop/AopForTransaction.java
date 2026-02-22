package com.deepflow.core.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분산락 AOP에서 새 트랜잭션을 열기 위한 헬퍼.
 * AOP 프록시는 자기 자신의 메서드에 @Transactional을 적용할 수 없어서
 * 별도 빈으로 분리하여 REQUIRES_NEW 전파를 보장.
 */
@Component
public class AopForTransaction {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
