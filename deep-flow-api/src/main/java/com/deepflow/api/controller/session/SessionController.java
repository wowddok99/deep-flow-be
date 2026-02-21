package com.deepflow.api.controller.session;

import com.deepflow.api.dto.*;
import com.deepflow.api.service.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session created"),
            @ApiResponse(responseCode = "409", description = "Ongoing session already exists")
    })
    @PostMapping("/start")
    public ResponseEntity<CommonResponse<SessionResponse>> startSession(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(sessionService.startSession(userDetails.getUserId())));
    }

    @Operation(summary = "Get All Sessions (Summary)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessions retrieved")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<CursorResponse<SessionSummaryResponse>>> getAllSessions(
            @Parameter(description = "Cursor ID for pagination") @RequestParam(required = false) Long cursorId,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(CommonResponse.ok(sessionService.getAllSessions(cursorId, size)));
    }

    @Operation(summary = "Get Session Detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session detail retrieved"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<SessionDetailResponse>> getSession(
            @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(CommonResponse.ok(sessionService.getSessionDetail(id)));
    }

    @Operation(summary = "Update Session Log")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Log updated"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PutMapping("/{id}/log")
    public ResponseEntity<CommonResponse<Void>> updateLog(
            @Parameter(description = "Session ID") @PathVariable Long id,
            @RequestBody @Valid LogUpdateRequest request
    ) {
        sessionService.updateLog(id, request);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Operation(summary = "Stop Focus Session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session stopped"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/{id}/stop")
    public ResponseEntity<CommonResponse<Void>> stopSession(
            @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        sessionService.stopSession(id);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Operation(summary = "Delete Session (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session deleted"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete ongoing session")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteSession(
            @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        sessionService.deleteSession(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
