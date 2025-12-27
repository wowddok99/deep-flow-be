package com.deepflow.api.dto;

import lombok.Builder;

@Builder
public record LogUpdateRequest(
                String content,
                String summary,
                String tags) {
}
