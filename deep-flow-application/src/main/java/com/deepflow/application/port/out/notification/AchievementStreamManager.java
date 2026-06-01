package com.deepflow.application.port.out.notification;

/**
 * 실시간 칭호 알림 SSE 연결 관리 포트
 *
 * 애플리케이션 계층이 웹 MVC 타입에 의존하지 않도록 반환 타입은 Object 로 유지
 */
public interface AchievementStreamManager {

    Object connect(Long userId);
}
