package com.deepflow.application.session.dto;

import com.deepflow.domain.session.comment.SessionComment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CommentInfo(
        Long id,
        Author user,
        String content,
        List<MentionedUser> mentions,
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

    /**
     * 본문에서 강조 표시할 멘션 정보. content 의 '@username' 패턴을 클라이언트가 이 목록과
     * 매칭해 chip 스타일로 렌더링한다.
     */
    public record MentionedUser(Long userId, String username, String name) {}

    public static CommentInfo fromTree(SessionComment c,
                                       Map<Long, List<SessionComment>> childrenByParent,
                                       Map<Long, List<MentionedUser>> mentionsByCommentId) {
        List<CommentInfo> replies = childrenByParent.getOrDefault(c.getId(), List.of()).stream()
                .map(child -> fromTree(child, childrenByParent, mentionsByCommentId))
                .toList();
        // soft-deleted 댓글은 본문이 마스킹되므로 멘션도 노출하지 않는다.
        List<MentionedUser> mentions = c.isDeleted()
                ? List.of()
                : mentionsByCommentId.getOrDefault(c.getId(), List.of());
        return new CommentInfo(
                c.getId(),
                Author.from(c.getUser()),
                c.isDeleted() ? "삭제된 댓글입니다" : c.getContent(),
                mentions,
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
                List.of(),
                c.isEdited(),
                c.isDeleted(),
                c.getCreatedAt(),
                List.of()
        );
    }
}
