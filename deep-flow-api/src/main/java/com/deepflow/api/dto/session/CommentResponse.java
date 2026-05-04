package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.CommentInfo;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        Author user,
        String content,
        List<Mention> mentions,
        boolean edited,
        boolean deleted,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
    public record Author(Long id, String name) {}

    /**
     * 본문 안의 '@username' 을 chip 스타일로 강조하기 위한 정보.
     * 클라이언트는 mentions 의 username 집합과 매칭되는 토큰만 강조 처리한다.
     */
    public record Mention(Long userId, String username, String name) {}

    public static CommentResponse from(CommentInfo info) {
        return new CommentResponse(
                info.id(),
                new Author(info.user().id(), info.user().name()),
                info.content(),
                info.mentions().stream()
                        .map(m -> new Mention(m.userId(), m.username(), m.name()))
                        .toList(),
                info.edited(),
                info.deleted(),
                info.createdAt(),
                info.replies().stream().map(CommentResponse::from).toList()
        );
    }
}
