package com.deepflow.api.mapper;

import com.deepflow.api.dto.SessionDetailResponse;
import com.deepflow.api.dto.SessionResponse;
import com.deepflow.api.dto.SessionSummaryResponse;
import com.deepflow.core.domain.log.FocusLogImage;
import com.deepflow.core.domain.session.FocusSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    SessionResponse toSessionResponse(FocusSession session);

    @Mapping(source = "focusLog.title", target = "title")
    @Mapping(source = "focusLog.summary", target = "summary")
    SessionSummaryResponse toSessionSummaryResponse(FocusSession session);

    @Mapping(source = "focusLog.content", target = "content")
    @Mapping(source = "focusLog.title", target = "title")
    @Mapping(source = "focusLog.summary", target = "summary")
    @Mapping(source = "focusLog.aiSummary", target = "aiSummary")
    @Mapping(source = "focusLog.images", target = "imageUrls")
    SessionDetailResponse toSessionDetailResponse(FocusSession session);

    default List<String> mapImages(List<FocusLogImage> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(FocusLogImage::getImageUrl)
                .toList();
    }

    default JsonNode toJsonNode(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(content);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
