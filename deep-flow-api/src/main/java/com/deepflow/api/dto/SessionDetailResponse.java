package com.deepflow.api.dto;

import com.deepflow.api.dto.SessionDetailResponse;
import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import java.time.LocalDateTime;

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
                String tags) {

        public static SessionDetailResponse from(FocusSession session) {
                return SessionDetailResponse.builder()
                                .id(session.getId())
                                .startTime(session.getStartTime())
                                .endTime(session.getEndTime())
                                .durationSeconds(session.getDurationSeconds())
                                .status(session.getStatus())
                                .content(session.getFocusLog().getContent())
                                .summary(session.getFocusLog().getSummary())
                                .tags(session.getFocusLog().getTags())
                                .build();
        }
}
