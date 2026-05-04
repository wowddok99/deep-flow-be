package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.TagSuggestResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.SessionTagService;
import com.deepflow.application.session.dto.TagSuggestInfo;
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

@Tag(name = "Crew Tag", description = "Crew tag suggestion API")
@Validated
@RestController
@RequestMapping("/api/v1/crews/{crewId}/tags")
@RequiredArgsConstructor
public class CrewTagController {

    private final SessionTagService tagService;

    @Operation(summary = "Popular tags in a crew (top N by frequency)")
    @GetMapping
    public ResponseEntity<CommonResponse<List<TagSuggestResponse>>> popularTags(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        List<TagSuggestInfo> infos = tagService.getPopularTags(user.getUserId(), crewId, limit);
        return ResponseEntity.ok(CommonResponse.ok(infos.stream().map(TagSuggestResponse::from).toList()));
    }

    @Operation(summary = "Suggest tags by prefix in a crew (autocomplete)")
    @GetMapping("/suggest")
    public ResponseEntity<CommonResponse<List<TagSuggestResponse>>> suggest(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestParam String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        List<TagSuggestInfo> infos = tagService.suggestTags(user.getUserId(), crewId, q, limit);
        return ResponseEntity.ok(CommonResponse.ok(infos.stream().map(TagSuggestResponse::from).toList()));
    }
}
