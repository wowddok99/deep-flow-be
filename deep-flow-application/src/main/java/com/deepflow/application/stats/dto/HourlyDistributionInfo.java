package com.deepflow.application.stats.dto;

public record HourlyDistributionInfo(
    int hour,
    long sessionCount
) {}
