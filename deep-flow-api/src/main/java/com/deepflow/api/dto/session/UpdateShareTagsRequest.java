package com.deepflow.api.dto.session;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateShareTagsRequest(
        @Size(max = 5, message = "Tags must be at most 5") List<String> tags
) {
    public List<String> safeTags() {
        return tags == null ? List.of() : tags;
    }
}
