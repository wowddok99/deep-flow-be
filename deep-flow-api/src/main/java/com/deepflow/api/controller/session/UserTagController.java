package com.deepflow.api.controller.session;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.SessionTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User Tag", description = "Personal tag history API")
@Validated
@RestController
@RequestMapping("/api/v1/users/me/tags")
@RequiredArgsConstructor
public class UserTagController {

    private final SessionTagService tagService;

    @Operation(summary = "My recently used tags (distinct, latest first)")
    @GetMapping("/recent")
    public ResponseEntity<CommonResponse<List<String>>> recent(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        List<String> tags = tagService.getMyRecentTags(user.getUserId(), limit);
        return ResponseEntity.ok(CommonResponse.ok(tags));
    }
}
