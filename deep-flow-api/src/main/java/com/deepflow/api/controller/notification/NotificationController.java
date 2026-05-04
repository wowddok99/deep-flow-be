package com.deepflow.api.controller.notification;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.CursorResponse;
import com.deepflow.api.dto.session.NotificationResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.common.SliceResult;
import com.deepflow.application.session.NotificationService;
import com.deepflow.application.session.dto.MentionInfo;
import com.deepflow.infra.notification.SseEmitterManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Tag(name = "Notification", description = "Comment / mention notification API")
@Validated
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    @Operation(summary = "Subscribe to comment / mention notification SSE stream")
    @GetMapping(value = "/comments/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails user) {
        return sseEmitterManager.connect(user.getUserId(), SseEmitterManager.Channel.COMMENT_NOTIFICATION);
    }

    @Operation(summary = "List unread mention notifications (cursor pagination)")
    @GetMapping("/unread")
    public ResponseEntity<CommonResponse<CursorResponse<NotificationResponse>>> unread(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @Min(1) Long cursorId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        SliceResult<MentionInfo> result = notificationService.getUnread(user.getUserId(), cursorId, size);
        List<NotificationResponse> content = result.content().stream().map(NotificationResponse::from).toList();
        CursorResponse<NotificationResponse> response = new CursorResponse<>(content, result.nextCursorId(), result.hasNext());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Mark a single notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable @Min(1) Long id
    ) {
        notificationService.markRead(user.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark all unread notifications as read")
    @PatchMapping("/read-all")
    public ResponseEntity<CommonResponse<Map<String, Integer>>> readAll(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int updated = notificationService.markAllRead(user.getUserId());
        return ResponseEntity.ok(CommonResponse.ok(Map.of("updated", updated)));
    }
}
