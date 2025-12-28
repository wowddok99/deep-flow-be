package com.deepflow.api.dto;

import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record SessionDetailResponse(
    Long id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationSeconds,
    SessionStatus status,
    String content,
    String summary,
    List<String> tags,
    List<String> imageUrls
) {
    public static SessionDetailResponse from(FocusSession session) {
        return SessionDetailResponse.builder()
            .id(session.getId())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .durationSeconds(session.getDurationSeconds())
            .status(session.getStatus())
            .content(session.getFocusLog().getContent())
            .summary(session.getFocusLog().getSummary())
            .tags(session.getFocusLog().getLogTags().stream()
                .map(logTag -> logTag.getTag().getName())
                .toList())
            .imageUrls(session.getFocusLog().getImages().stream()
                .map(image -> image.getImageUrl())
                .toList())
            .build();
    }
}
