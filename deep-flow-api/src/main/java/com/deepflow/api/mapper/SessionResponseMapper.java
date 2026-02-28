package com.deepflow.api.mapper;

import com.deepflow.api.dto.SessionDetailResponse;
import com.deepflow.application.session.dto.SessionDetailInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionResponseMapper {

    private final ObjectMapper objectMapper;

    public SessionDetailResponse toDetailResponse(SessionDetailInfo info) {
        JsonNode contentNode = parseContent(info.id(), info.content());
        return SessionDetailResponse.from(info, contentNode);
    }

    private JsonNode parseContent(Long sessionId, String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            log.warn("세션 콘텐츠 JSON 파싱 실패: sessionId={}, error={}", sessionId, e.getMessage());
            return null;
        }
    }
}
