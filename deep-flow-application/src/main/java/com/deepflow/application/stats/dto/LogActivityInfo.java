package com.deepflow.application.stats.dto;

public record LogActivityInfo(
    long totalLogs,
    long totalImages,
    int avgContentLength
) {}
