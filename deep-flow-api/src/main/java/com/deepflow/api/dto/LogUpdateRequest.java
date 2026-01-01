package com.deepflow.api.dto;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;

import java.util.List;
import java.util.Map;

@Builder
public record LogUpdateRequest(
    Map<String, Object> content,
    String title,
    String summary,
    List<String> imageUrls
) {
    @JsonIgnore
    @AssertTrue(message = "Invalid format")
    public boolean isValidDocument() {
        if (content == null || content.isEmpty()) {
            return true;
        }
        return "doc".equals(content.get("type"));
    }
}
