package com.deepflow.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Log update request")
public record LogUpdateRequest(
    @Schema(description = "TipTap editor content (JSON)")
    JsonNode content,
    @Schema(description = "Log title", example = "Spring Boot study")
    String title,
    @Schema(description = "Log summary", example = "Studied JPA and Spring Security")
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
