package com.deepflow.api.controller.session;

import com.deepflow.api.dto.*;
import com.deepflow.api.service.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Focus Session", description = "Focus Session & Logging API")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Start Focus Session")
    @PostMapping("/start")
    public SessionResponse startSession() {
        return sessionService.startSession();
    }

    @Operation(summary = "Get All Sessions (Summary)")
    @GetMapping
    public List<SessionSummaryResponse> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @Operation(summary = "Get Session Detail")
    @GetMapping("/{id}")
    public SessionDetailResponse getSession(@PathVariable Long id) {
        return sessionService.getSessionDetail(id);
    }

    @Operation(summary = "Update Session Log")
    @PutMapping("/{id}/log")
    public void updateLog(@PathVariable Long id, @RequestBody LogUpdateRequest request) {
        sessionService.updateLog(id, request);
    }

    @Operation(summary = "Stop Focus Session")
    @PostMapping("/{id}/stop")
    public void stopSession(@PathVariable Long id) {
        sessionService.stopSession(id);
    }
}
