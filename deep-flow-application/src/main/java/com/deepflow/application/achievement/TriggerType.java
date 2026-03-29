package com.deepflow.application.achievement;

/** 칭호 평가가 실행되는 시점 유형 */
public enum TriggerType {
    SESSION_STOP,    // 세션 종료 시 (기존)
    LOG_UPDATE,      // 로그(제목/본문/이미지/요약) 업데이트 시
    TIME_CHECK       // 스케줄러에 의한 주기적 시간 체크 시
}
