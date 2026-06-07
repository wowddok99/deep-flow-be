package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.LivePresenceResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.crew.presence.CrewLivePresenceService;
import com.deepflow.application.crew.dto.LivePresenceInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Crew Live Presence", description = "Crew live presence snapshot (page entry)")
@Validated
@RestController
@RequestMapping("/api/v1/crews/{crewId}/presence")
@RequiredArgsConstructor
public class CrewLivePresenceController {

    private final CrewLivePresenceService livePresenceService;

    @Operation(summary = "Get live presence snapshot for a crew (active members)")
    @GetMapping("/live")
    public ResponseEntity<CommonResponse<LivePresenceResponse>> live(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId
    ) {
        LivePresenceInfo info = livePresenceService.getLivePresence(user.getUserId(), crewId);
        return ResponseEntity.ok(CommonResponse.ok(LivePresenceResponse.from(info)));
    }
}
