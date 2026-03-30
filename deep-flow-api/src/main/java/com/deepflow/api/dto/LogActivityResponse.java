package com.deepflow.api.dto;

import com.deepflow.application.stats.dto.LogActivityInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Log activity summary")
public record LogActivityResponse(
        long totalLogs,
        long totalImages,
        int avgContentLength
) {
    public static LogActivityResponse from(LogActivityInfo info) {
        return new LogActivityResponse(info.totalLogs(), info.totalImages(), info.avgContentLength());
    }
}
