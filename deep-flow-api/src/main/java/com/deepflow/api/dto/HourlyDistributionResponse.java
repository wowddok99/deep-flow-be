package com.deepflow.api.dto;

import com.deepflow.application.stats.dto.HourlyDistributionInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Hourly session distribution")
public record HourlyDistributionResponse(
        int hour,
        long sessionCount
) {
    public static HourlyDistributionResponse from(HourlyDistributionInfo info) {
        return new HourlyDistributionResponse(info.hour(), info.sessionCount());
    }
}
