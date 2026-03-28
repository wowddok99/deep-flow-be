package com.deepflow.api.controller.achievement;

import com.deepflow.api.dto.*;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.achievement.AchievementService;
import com.deepflow.application.achievement.dto.AchievementInfo;
import com.deepflow.application.achievement.dto.UserAchievementInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Achievement", description = "Achievement API")
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "Get all achievements with user progress")
    @GetMapping
    public ResponseEntity<CommonResponse<List<AchievementResponse>>> getAllAchievements(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AchievementInfo> infos = achievementService.getAllAchievements(userDetails.getUserId());
        List<AchievementResponse> responses = infos.stream()
                .map(AchievementResponse::from)
                .toList();
        return ResponseEntity.ok(CommonResponse.ok(responses));
    }

    @Operation(summary = "Get my achieved titles")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<List<UserAchievementResponse>>> getMyAchievements(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserAchievementInfo> infos = achievementService.getMyAchievements(userDetails.getUserId());
        List<UserAchievementResponse> responses = infos.stream()
                .map(UserAchievementResponse::from)
                .toList();
        return ResponseEntity.ok(CommonResponse.ok(responses));
    }

    @Operation(summary = "Update display achievement")
    @PutMapping("/display")
    public ResponseEntity<CommonResponse<Void>> updateDisplayAchievement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid DisplayAchievementRequest request
    ) {
        achievementService.updateDisplayAchievement(userDetails.getUserId(), request.achievementCode());
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
