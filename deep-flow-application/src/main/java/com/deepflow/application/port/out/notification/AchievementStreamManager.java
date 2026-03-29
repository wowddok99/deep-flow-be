package com.deepflow.application.port.out.notification;

/**
 * 실시간 칭호 알림을 위한 SSE 연결 관리 포트.
 * Controller가 infra 직접 의존하지 않도록 application 레이어에 인터페이스 정의.
 * 반환 타입은 Object로 선언하여 application 레이어의 spring-webmvc 의존을 방지.
 * 실제 구현체(infra)에서 SseEmitter를 반환하며, Controller(api)에서 캐스팅.
 */
public interface AchievementStreamManager {

    Object connect(Long userId);
}
