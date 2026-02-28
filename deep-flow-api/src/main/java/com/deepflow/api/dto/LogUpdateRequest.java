package com.deepflow.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Log update request")
public record LogUpdateRequest(
    @Schema(description = "TipTap editor content (JSON)")
    JsonNode content,
    @Schema(description = "Log title (max 100 chars)", example = "Spring Boot study")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    String title,
    @Schema(description = "Log summary (max 500 chars)", example = "Studied JPA and Spring Security")
    @Size(max = 500, message = "Summary must not exceed 500 characters")
    String summary,
    @Schema(description = "Attached image URLs")
    List<String> imageUrls
) {
    @JsonIgnore
    @AssertTrue(message = "Invalid format")
    public boolean isValidDocument() {
        if (content == null || content.isEmpty()) {
            return true;
        }
        return content.has("type") && "doc".equals(content.get("type").asText());
    }
}
