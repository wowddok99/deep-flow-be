package com.deepflow.api.controller.session;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.ReactionAggregateResponse;
import com.deepflow.api.dto.session.ReactionToggleRequest;
import com.deepflow.api.dto.session.ReactionToggleResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.SessionReactionService;
import com.deepflow.application.session.dto.ReactionAggregateInfo;
import com.deepflow.application.session.dto.ReactionToggleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Session Reaction", description = "Crew shared session reaction API")
@Validated
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/reactions")
@RequiredArgsConstructor
public class SessionReactionController {

    private final SessionReactionService reactionService;

    @Operation(summary = "Toggle reaction (add if absent, remove if present)")
    @PostMapping
    public ResponseEntity<CommonResponse<ReactionToggleResponse>> toggle(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId,
            @RequestBody @Valid ReactionToggleRequest request
    ) {
        ReactionToggleResult result = reactionService.toggle(user.getUserId(), sessionId, request.emoji());
        return ResponseEntity.ok(CommonResponse.ok(ReactionToggleResponse.from(result)));
    }

    @Operation(summary = "Aggregate reactions of a session")
    @GetMapping
    public ResponseEntity<CommonResponse<ReactionAggregateResponse>> aggregate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId
    ) {
        ReactionAggregateInfo info = reactionService.aggregate(user.getUserId(), sessionId);
        return ResponseEntity.ok(CommonResponse.ok(ReactionAggregateResponse.from(info)));
    }
}
