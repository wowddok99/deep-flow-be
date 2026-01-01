package com.deepflow.api.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken
) {
}
