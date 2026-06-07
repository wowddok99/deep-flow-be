package com.deepflow.api.dto.session;

import com.deepflow.application.crew.dto.CrewSessionDetailInfo;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public record CrewSessionDetailResponse(
        Long sessionId,
        UserBrief user,
        String title,
        // 프론트에서 Tiptap setContent 에 바로 넣을 수 있도록 파싱된 JSON 트리로 노출
        JsonNode content,
        Long durationSeconds,
        LocalDateTime sharedAt,
        List<String> tags,
        int reactionCount,
        int commentCount,
        boolean edited
) {
    public static CrewSessionDetailResponse from(CrewSessionDetailInfo info, JsonNode contentNode) {
        return new CrewSessionDetailResponse(
                info.sessionId(),
                new UserBrief(info.userId(), info.userName()),
                info.title(),
                contentNode,
                info.durationSeconds(),
                info.sharedAt(),
                info.tags(),
                info.reactionCount(),
                info.commentCount(),
                info.edited()
        );
    }

    public record UserBrief(Long id, String name) {}
}
