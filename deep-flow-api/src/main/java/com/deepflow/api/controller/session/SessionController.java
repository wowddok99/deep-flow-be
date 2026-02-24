package com.deepflow.api.controller.session;

import com.deepflow.api.dto.*;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.common.SliceResult;
import com.deepflow.application.session.SessionService;
import com.deepflow.application.session.dto.SessionDetailInfo;
import com.deepflow.application.session.dto.SessionInfo;
import com.deepflow.application.session.dto.SessionSummaryInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        SessionInfo info = sessionService.startSession(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(SessionResponse.from(info)));
    }

    @Operation(summary = "Get All Sessions (Summary)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessions retrieved")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<CursorResponse<SessionSummaryResponse>>> getAllSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Cursor ID for pagination") @RequestParam(required = false) Long cursorId,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        SliceResult<SessionSummaryInfo> result = sessionService.getAllSessions(userDetails.getUserId(), cursorId, size);

        List<SessionSummaryResponse> content = result.content().stream()
                .map(SessionSummaryResponse::from)
                .toList();

        CursorResponse<SessionSummaryResponse> response = new CursorResponse<>(content, result.nextCursorId(), result.hasNext());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get Session Detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session detail retrieved"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<SessionDetailResponse>> getSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        SessionDetailInfo info = sessionService.getSessionDetail(userDetails.getUserId(), id);
        return ResponseEntity.ok(CommonResponse.ok(SessionDetailResponse.from(info)));
    }

    @Operation(summary = "Update Session Log")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Log updated"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PutMapping("/{id}/log")
    public ResponseEntity<CommonResponse<Void>> updateLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Session ID") @PathVariable Long id,
            @RequestBody @Valid LogUpdateRequest request
    ) {
        String contentStr = request.content() != null ? request.content().toString() : null;
        sessionService.updateLog(userDetails.getUserId(), id, request.title(), contentStr, request.summary(), request.imageUrls());
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Operation(summary = "Stop Focus Session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session stopped"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/{id}/stop")
    public ResponseEntity<CommonResponse<Void>> stopSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        sessionService.stopSession(userDetails.getUserId(), id);
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        sessionService.deleteSession(userDetails.getUserId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
