package com.deepflow.api.controller.session;

import com.deepflow.api.dto.*;
import com.deepflow.api.dto.CursorResponse;
import com.deepflow.api.service.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.deepflow.api.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Focus Session", description = "Focus Session & Logging API")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Start Focus Session")
    @PostMapping("/start")
    public SessionResponse startSession(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return sessionService.startSession(userDetails.getUserId());
    }

    @Operation(summary = "Get All Sessions (Summary)")
    @GetMapping
    public CursorResponse<SessionSummaryResponse> getAllSessions(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        return sessionService.getAllSessions(cursorId, size);
    }

    @Operation(summary = "Get Session Detail")
    @GetMapping("/{id}")
    public SessionDetailResponse getSession(@PathVariable Long id) {
        return sessionService.getSessionDetail(id);
    }

    @Operation(summary = "Update Session Log")
    @PutMapping("/{id}/log")
    public void updateLog(@PathVariable Long id, @RequestBody @Valid LogUpdateRequest request) {
        sessionService.updateLog(id, request);
    }

    @Operation(summary = "Stop Focus Session")
    @PostMapping("/{id}/stop")
    public void stopSession(@PathVariable Long id) {
        sessionService.stopSession(id);
    }

    @Operation(summary = "Delete Session")
    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
    }
}
