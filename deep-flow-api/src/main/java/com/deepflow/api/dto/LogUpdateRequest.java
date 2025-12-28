package com.deepflow.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LogUpdateRequest(
    Object content,
    String summary,
    List<String> tags,
    List<String> imageUrls
) {}
