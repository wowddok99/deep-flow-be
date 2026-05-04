package com.deepflow.api.controller.session;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.CommentResponse;
import com.deepflow.api.dto.session.UpdateCommentRequest;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.SessionCommentService;
import com.deepflow.application.session.dto.CommentInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "Comment edit/delete API")
@Validated
@RestController
@RequestMapping("/api/v1/comments/{commentId}")
@RequiredArgsConstructor
public class CommentController {

    private final SessionCommentService commentService;

    @Operation(summary = "Update own comment")
    @PatchMapping
    public ResponseEntity<CommonResponse<CommentResponse>> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long commentId,
            @RequestBody @Valid UpdateCommentRequest request
    ) {
        CommentInfo info = commentService.update(user.getUserId(), commentId, request.content());
        return ResponseEntity.ok(CommonResponse.ok(CommentResponse.from(info)));
    }

    @Operation(summary = "Soft-delete own comment")
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long commentId
    ) {
        commentService.delete(user.getUserId(), commentId);
        return ResponseEntity.noContent().build();
    }
}
