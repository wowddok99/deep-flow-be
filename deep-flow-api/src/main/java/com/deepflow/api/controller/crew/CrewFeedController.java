package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.CursorTokenResponse;
import com.deepflow.api.dto.session.CrewFeedItemResponse;
import com.deepflow.api.dto.session.CrewSessionDetailResponse;
import com.deepflow.api.mapper.SessionResponseMapper;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.crew.feed.CrewFeedService;
import com.deepflow.application.crew.dto.CrewFeedItemInfo;
import com.deepflow.application.crew.dto.CrewSessionDetailInfo;
import com.deepflow.application.crew.dto.SharedFeedSlice;
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
    private final SessionResponseMapper sessionResponseMapper;

    @Operation(summary = "List shared sessions in a crew (cursor pagination)")
    @GetMapping("/feed")
    public ResponseEntity<CommonResponse<CursorTokenResponse<CrewFeedItemResponse>>> feed(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String tag
    ) {
        SharedFeedSlice<CrewFeedItemInfo> result = crewFeedService.getFeed(user.getUserId(), crewId, cursor, size, tag);
        List<CrewFeedItemResponse> content = result.content().stream().map(CrewFeedItemResponse::from).toList();
        CursorTokenResponse<CrewFeedItemResponse> response = new CursorTokenResponse<>(content, result.nextCursor(), result.hasNext());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get a shared session detail in a crew")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<CommonResponse<CrewSessionDetailResponse>> getSharedSession(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @PathVariable @Min(1) Long sessionId
    ) {
        CrewSessionDetailInfo info = crewFeedService.getSharedSession(user.getUserId(), crewId, sessionId);
        return ResponseEntity.ok(CommonResponse.ok(sessionResponseMapper.toCrewDetailResponse(info)));
    }
}
