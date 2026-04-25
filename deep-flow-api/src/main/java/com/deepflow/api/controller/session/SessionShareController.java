package com.deepflow.api.controller.session;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.ShareSessionRequest;
import com.deepflow.api.dto.session.SharedSessionResponse;
import com.deepflow.api.dto.session.UpdateShareTagsRequest;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.SessionShareLocker;
import com.deepflow.application.session.dto.SharedSessionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Session Share", description = "Crew shared session API")
@Validated
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/share")
@RequiredArgsConstructor
public class SessionShareController {

    private final SessionShareLocker shareLocker;

    @Operation(summary = "Share a session to a crew with optional tags")
    @PostMapping
    public ResponseEntity<CommonResponse<SharedSessionResponse>> share(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId,
            @RequestBody @Valid ShareSessionRequest request
    ) {
        SharedSessionInfo info = shareLocker.share(user.getUserId(), sessionId, request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(SharedSessionResponse.from(info)));
    }

    @Operation(summary = "Replace tags of a shared session")
    @PutMapping("/tags")
    public ResponseEntity<CommonResponse<SharedSessionResponse>> updateTags(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId,
            @RequestBody @Valid UpdateShareTagsRequest request
    ) {
        SharedSessionInfo info = shareLocker.updateTags(user.getUserId(), sessionId, request.safeTags());
        return ResponseEntity.ok(CommonResponse.ok(SharedSessionResponse.from(info)));
    }

    @Operation(summary = "Unshare a session (soft delete, no restore)")
    @DeleteMapping
    public ResponseEntity<Void> unshare(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId
    ) {
        shareLocker.unshare(user.getUserId(), sessionId);
        return ResponseEntity.noContent().build();
    }
}
