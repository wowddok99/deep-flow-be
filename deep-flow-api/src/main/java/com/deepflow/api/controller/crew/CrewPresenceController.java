package com.deepflow.api.controller.crew;

import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.infra.notification.SseEmitterManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Crew Presence SSE", description = "Real-time crew presence stream")
@RestController
@RequestMapping("/api/v1/crews/presence")
@RequiredArgsConstructor
public class CrewPresenceController {

    private final SseEmitterManager sseEmitterManager;

    @Operation(summary = "Subscribe to crew presence SSE stream")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails user) {
        return sseEmitterManager.connect(user.getUserId());
    }
}
