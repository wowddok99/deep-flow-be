package com.deepflow.api.controller.session;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.session.CommentResponse;
import com.deepflow.api.dto.session.CreateCommentRequest;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.session.comment.SessionCommentService;
import com.deepflow.application.session.comment.dto.CommentInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Session Comment", description = "Crew shared session comment API")
@Validated
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/comments")
@RequiredArgsConstructor
public class SessionCommentController {

    private final SessionCommentService commentService;

    @Operation(summary = "List comments of a session (tree)")
    @GetMapping
    public ResponseEntity<CommonResponse<List<CommentResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId
    ) {
        List<CommentInfo> tree = commentService.getComments(user.getUserId(), sessionId);
        return ResponseEntity.ok(CommonResponse.ok(tree.stream().map(CommentResponse::from).toList()));
    }

    @Operation(summary = "Create a comment (root or reply)")
    @PostMapping
    public ResponseEntity<CommonResponse<CommentResponse>> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long sessionId,
            @RequestBody @Valid CreateCommentRequest request
    ) {
        CommentInfo info = commentService.create(user.getUserId(), sessionId, request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(CommentResponse.from(info)));
    }
}
