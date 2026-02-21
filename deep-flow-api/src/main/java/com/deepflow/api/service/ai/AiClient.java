package com.deepflow.api.service.ai;

/**
 * AI 요약 클라이언트 인터페이스.
 * 로컬 AI 등 구현체를 추가 예정.
 */
public interface AiClient {

    String summarize(String content);
}
