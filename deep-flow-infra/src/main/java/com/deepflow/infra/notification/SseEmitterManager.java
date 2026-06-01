package com.deepflow.infra.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자와 채널 단위로 SSE 연결 생명주기 관리
 *
 * 같은 사용자가 칭호, 크루 프레즌스, 댓글 알림 채널을 동시에 유지할 수 있도록 키 분리
 */
@Slf4j
@Component
public class SseEmitterManager {

    public enum Channel { ACHIEVEMENT, CREW_PRESENCE, COMMENT_NOTIFICATION }

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    private String key(Long userId, Channel channel) {
        return userId + ":" + channel.name();
    }

    public SseEmitter connect(Long userId, Channel channel) {
        String compositeKey = key(userId, channel);

        // 같은 채널의 이전 연결만 닫아 다른 SSE 채널은 유지
        SseEmitter existing = emitters.remove(compositeKey);
        if (existing != null) {
            existing.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(compositeKey);
            log.debug("SSE completed: key={}", compositeKey);
        });
        emitter.onTimeout(() -> {
            emitters.remove(compositeKey);
            log.debug("SSE timeout: key={}", compositeKey);
        });
        emitter.onError(e -> {
            emitters.remove(compositeKey);
            log.debug("SSE error: key={}", compositeKey);
        });

        emitters.put(compositeKey, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitters.remove(compositeKey);
        }

        log.info("SSE connected: key={}", compositeKey);
        return emitter;
    }

    public void send(Long userId, Channel channel, String eventName, Object data) {
        String compositeKey = key(userId, channel);
        SseEmitter emitter = emitters.get(compositeKey);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            emitters.remove(compositeKey);
            log.warn("SSE send failed, removed: key={}", compositeKey);
        }
    }

    public boolean isConnected(Long userId, Channel channel) {
        return emitters.containsKey(key(userId, channel));
    }
}
