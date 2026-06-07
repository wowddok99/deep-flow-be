package com.deepflow.api.dto.session;

import com.deepflow.application.session.comment.dto.CommentInfo;

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
     * 댓글 본문에서 실제 멤버 멘션만 강조하기 위한 사용자 정보
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
