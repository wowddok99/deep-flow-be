package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.CommentInfo;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        Author user,
        String content,
        boolean edited,
        boolean deleted,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
    public record Author(Long id, String name) {}

    public static CommentResponse from(CommentInfo info) {
        return new CommentResponse(
                info.id(),
                new Author(info.user().id(), info.user().name()),
                info.content(),
                info.edited(),
                info.deleted(),
                info.createdAt(),
                info.replies().stream().map(CommentResponse::from).toList()
        );
    }
}
