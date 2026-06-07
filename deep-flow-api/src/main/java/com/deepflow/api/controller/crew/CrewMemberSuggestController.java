package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.crew.MemberSuggestResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.crew.member.CrewMemberSuggestService;
import com.deepflow.application.crew.dto.MemberSuggestionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Crew Member Suggest", description = "Mention autocomplete API")
@Validated
@RestController
@RequestMapping("/api/v1/crews/{crewId}/members")
@RequiredArgsConstructor
public class CrewMemberSuggestController {

    private final CrewMemberSuggestService suggestService;

    @Operation(summary = "Suggest crew members by username/name prefix (mention autocomplete)")
    @GetMapping("/suggest")
    public ResponseEntity<CommonResponse<List<MemberSuggestResponse>>> suggest(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestParam String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        List<MemberSuggestionInfo> infos = suggestService.suggestMembers(user.getUserId(), crewId, q, limit);
        return ResponseEntity.ok(CommonResponse.ok(infos.stream().map(MemberSuggestResponse::from).toList()));
    }
}
