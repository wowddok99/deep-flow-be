package com.deepflow.infra.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** userId별 SseEmitter 생명주기 관리 (연결, 전송, 정리) */
@Slf4j
@Component
public class SseEmitterManager {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    public SseEmitter connect(Long userId) {
        // 기존 연결이 있으면 닫고 교체 (한 유저당 하나만 유지)
        SseEmitter existing = emitters.remove(userId);
        if (existing != null) {
            existing.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.debug("SSE completed: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.debug("SSE timeout: userId={}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.debug("SSE error: userId={}", userId);
        });

        emitters.put(userId, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        log.info("SSE connected: userId={}", userId);
        return emitter;
    }

    public void send(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            emitters.remove(userId);
            log.warn("SSE send failed, removed: userId={}", userId);
        }
    }

    public boolean isConnected(Long userId) {
        return emitters.containsKey(userId);
    }
}
