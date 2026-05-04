package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.CursorResponse;
import com.deepflow.api.dto.crew.*;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.common.SliceResult;
import com.deepflow.application.crew.CrewService;
import com.deepflow.application.crew.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Crew", description = "Crew (focus group) API")
@Validated
@RestController
@RequestMapping("/api/v1/crews")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    @Operation(summary = "Create crew")
    @PostMapping
    public ResponseEntity<CommonResponse<CrewResponse>> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid CreateCrewRequest request
    ) {
        CrewSummaryInfo info = crewService.create(user.getUserId(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(CrewResponse.from(info)));
    }

    @Operation(summary = "List my crews")
    @GetMapping
    public ResponseEntity<CommonResponse<List<CrewResponse>>> listMine(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<CrewSummaryInfo> list = crewService.listMyCrews(user.getUserId());
        return ResponseEntity.ok(CommonResponse.ok(list.stream().map(CrewResponse::from).toList()));
    }

    @Operation(summary = "Get crew detail")
    @GetMapping("/{crewId}")
    public ResponseEntity<CommonResponse<CrewDetailResponse>> detail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        CrewDetailInfo info = crewService.getDetail(user.getUserId(), crewId);
        return ResponseEntity.ok(CommonResponse.ok(CrewDetailResponse.from(info)));
    }

    @Operation(summary = "Search public crews")
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<CursorResponse<CrewResponse>>> search(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String q,
            @RequestParam(required = false) @Min(1) Long cursorId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        SliceResult<CrewSummaryInfo> result = crewService.searchPublic(user.getUserId(), q, cursorId, size);
        List<CrewResponse> content = result.content().stream().map(CrewResponse::from).toList();
        CursorResponse<CrewResponse> response = new CursorResponse<>(content, result.nextCursorId(), result.hasNext());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Update crew")
    @PatchMapping("/{crewId}")
    public ResponseEntity<CommonResponse<CrewResponse>> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestBody @Valid UpdateCrewRequest request
    ) {
        CrewSummaryInfo info = crewService.update(user.getUserId(), crewId, request.toCommand());
        return ResponseEntity.ok(CommonResponse.ok(CrewResponse.from(info)));
    }

    @Operation(summary = "Disband crew (owner)")
    @DeleteMapping("/{crewId}")
    public ResponseEntity<Void> disband(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        crewService.disband(user.getUserId(), crewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Issue invite code")
    @PostMapping("/{crewId}/invite")
    public ResponseEntity<CommonResponse<InviteCodeResponse>> issueInvite(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestBody @Valid IssueInviteCodeRequest request
    ) {
        InviteCodeIssuedInfo info = crewService.issueInviteCode(user.getUserId(), crewId, request.ttlMinutes());
        return ResponseEntity.ok(CommonResponse.ok(InviteCodeResponse.from(info)));
    }

    @Operation(summary = "Join crew by invite code")
    @PostMapping("/join")
    public ResponseEntity<CommonResponse<CrewResponse>> joinByCode(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid JoinByCodeRequest request
    ) {
        CrewSummaryInfo info = crewService.joinByCode(user.getUserId(), request.code());
        return ResponseEntity.ok(CommonResponse.ok(CrewResponse.from(info)));
    }

    @Operation(summary = "Join public crew directly")
    @PostMapping("/{crewId}/join")
    public ResponseEntity<CommonResponse<CrewResponse>> joinPublic(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        CrewSummaryInfo info = crewService.joinPublic(user.getUserId(), crewId);
        return ResponseEntity.ok(CommonResponse.ok(CrewResponse.from(info)));
    }

    @Operation(summary = "Leave crew")
    @DeleteMapping("/{crewId}/members/me")
    public ResponseEntity<Void> leave(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        crewService.leave(user.getUserId(), crewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Kick crew member (owner)")
    @DeleteMapping("/{crewId}/members/{userId}")
    public ResponseEntity<Void> kick(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @PathVariable @Min(1) Long userId
    ) {
        crewService.kick(user.getUserId(), crewId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get crew activity")
    @GetMapping("/{crewId}/activity")
    public ResponseEntity<CommonResponse<CrewActivityResponse>> activity(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        CrewActivityInfo info = crewService.getActivity(user.getUserId(), crewId);
        return ResponseEntity.ok(CommonResponse.ok(CrewActivityResponse.from(info)));
    }
}
