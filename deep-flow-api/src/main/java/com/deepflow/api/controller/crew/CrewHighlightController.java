package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.CrewHighlightResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.CrewHighlightService;
import com.deepflow.application.session.dto.CrewHighlightInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Crew Highlight", description = "Crew adaptive highlight API")
@Validated
@RestController
@RequestMapping("/api/v1/crews/{crewId}/highlights")
@RequiredArgsConstructor
public class CrewHighlightController {

    private final CrewHighlightService highlightService;

    @Operation(summary = "Adaptive crew highlight (EMPTY/GROWING/MATURE)")
    @GetMapping
    public ResponseEntity<CommonResponse<CrewHighlightResponse>> highlight(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        CrewHighlightInfo info = highlightService.getHighlight(user.getUserId(), crewId);
        return ResponseEntity.ok(CommonResponse.ok(CrewHighlightResponse.from(info)));
    }
}
