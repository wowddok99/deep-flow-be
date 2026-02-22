package com.deepflow.api.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    // controller 패키지 하위의 모든 메서드 실행 시점에 적용
    @Around("execution(* com.deepflow.api.controller..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        // 메서드 실행 시작 시간 기록
        long start = System.currentTimeMillis();

        // 실제 대상 메서드 실행
        Object proceed = joinPoint.proceed();

        // 실행 시간 계산
        long executionTime = System.currentTimeMillis() - start;

        // 메서드 시그니처와 실행 시간 로깅
        log.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);
        return proceed;
    }
}
