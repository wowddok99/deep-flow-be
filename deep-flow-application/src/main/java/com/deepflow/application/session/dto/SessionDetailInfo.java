package com.deepflow.application.session.dto;

import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.log.FocusLogImage;
import com.deepflow.domain.session.FocusSession;

import java.time.LocalDateTime;
import java.util.List;

public record SessionDetailInfo(
    Long id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationSeconds,
    String status,
    String title,
    String content,
    String summary,
    List<String> imageUrls
) {
    public static SessionDetailInfo from(FocusSession session) {
        FocusLog log = session.getFocusLog();
        List<String> urls = (log.getImages() != null)
            ? log.getImages().stream().map(FocusLogImage::getImageUrl).toList()
            : List.of();

        return new SessionDetailInfo(
            session.getId(),
            session.getStartTime(),
            session.getEndTime(),
            session.getDurationSeconds(),
            session.getStatus().name(),
            log.getTitle(),
            log.getContent(),
            log.getSummary(),
            urls
        );
    }
}
