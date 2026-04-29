package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 불투명 토큰 기반 커서 페이지네이션 응답.
 * 클라이언트는 nextCursor 문자열을 그대로 다음 요청의 cursor 파라미터로 전달하면 됨.
 */
@Schema(description = "Opaque-cursor pagination response")
public record CursorTokenResponse<T>(
        @Schema(description = "Content list")
        List<T> content,

        @Schema(description = "Next opaque cursor token (null if no more)",
                example = "MjAyNi0wNC0yOVQyMzoyNDowMHw2Nw")
        String nextCursor,

        @Schema(description = "Whether more data exists")
        boolean hasNext
) {}
