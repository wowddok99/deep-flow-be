package com.deepflow.application.session.dto;

import com.deepflow.domain.session.comment.SessionComment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CommentInfo(
        Long id,
        Author user,
        String content,
        boolean edited,
        boolean deleted,
        LocalDateTime createdAt,
        List<CommentInfo> replies
) {
    public record Author(Long id, String name) {
        public static Author from(com.deepflow.domain.user.User u) {
            return u == null ? new Author(null, "알수없음") : new Author(u.getId(), u.getName());
        }
    }

    public static CommentInfo fromTree(SessionComment c, Map<Long, List<SessionComment>> childrenByParent) {
        List<CommentInfo> replies = childrenByParent.getOrDefault(c.getId(), List.of()).stream()
                .map(child -> fromTree(child, childrenByParent))
                .toList();
        return new CommentInfo(
                c.getId(),
                Author.from(c.getUser()),
                c.isDeleted() ? "삭제된 댓글입니다" : c.getContent(),
                c.isEdited(),
                c.isDeleted(),
                c.getCreatedAt(),
                replies
        );
    }

    public static CommentInfo singleNonTree(SessionComment c) {
        return new CommentInfo(
                c.getId(),
                Author.from(c.getUser()),
                c.isDeleted() ? "삭제된 댓글입니다" : c.getContent(),
                c.isEdited(),
                c.isDeleted(),
                c.getCreatedAt(),
                List.of()
        );
    }
}
