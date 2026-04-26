package com.deepflow.api.controller.crew;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.SearchResultResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.SearchService;
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
import java.util.Map;

@Tag(name = "Crew Search", description = "Crew shared session search API")
@Validated
@RestController
@RequestMapping("/api/v1/crews/{crewId}/search")
@RequiredArgsConstructor
public class CrewSearchController {

    private final SearchService searchService;

    @Operation(summary = "Search shared sessions in a crew (full-text)")
    @GetMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> search(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long crewId,
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "session") String type,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        List<SearchResultResponse> items = searchService.search(user.getUserId(), crewId, q, type, offset, size).stream()
                .map(SearchResultResponse::from)
                .toList();
        return ResponseEntity.ok(CommonResponse.ok(Map.of(
                "items", items,
                "offset", offset,
                "size", size,
                "hasNext", items.size() == size
        )));
    }
}
