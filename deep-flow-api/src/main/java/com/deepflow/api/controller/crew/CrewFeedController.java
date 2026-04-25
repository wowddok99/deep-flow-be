package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.CursorResponse;
import com.deepflow.api.dto.session.CrewFeedItemResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.common.SliceResult;
import com.deepflow.application.session.CrewFeedService;
import com.deepflow.application.session.dto.CrewFeedItemInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Crew Feed", description = "Crew shared session feed API")
@Validated
@RestController
@RequestMapping("/api/v1/crews/{crewId}")
@RequiredArgsConstructor
public class CrewFeedController {

    private final CrewFeedService crewFeedService;

    @Operation(summary = "List shared sessions in a crew (cursor pagination)")
    @GetMapping("/feed")
    public ResponseEntity<CommonResponse<CursorResponse<CrewFeedItemResponse>>> feed(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestParam(required = false) @Min(1) Long cursorId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String tag
    ) {
        SliceResult<CrewFeedItemInfo> result = crewFeedService.getFeed(user.getUserId(), crewId, cursorId, size, tag);
        List<CrewFeedItemResponse> content = result.content().stream().map(CrewFeedItemResponse::from).toList();
        CursorResponse<CrewFeedItemResponse> response = new CursorResponse<>(content, result.nextCursorId(), result.hasNext());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get a shared session detail in a crew")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<CommonResponse<CrewFeedItemResponse>> getSharedSession(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @PathVariable @Min(1) Long sessionId
    ) {
        CrewFeedItemInfo info = crewFeedService.getSharedSession(user.getUserId(), crewId, sessionId);
        return ResponseEntity.ok(CommonResponse.ok(CrewFeedItemResponse.from(info)));
    }
}
