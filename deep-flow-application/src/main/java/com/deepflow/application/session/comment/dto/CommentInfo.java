package com.deepflow.application.session.comment.dto;

import com.deepflow.domain.session.comment.SessionComment;
import com.deepflow.domain.user.User;

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
    private static final String DELETED_COMMENT_MESSAGE = "삭제된 댓글입니다";

    public record Author(Long id, String name) {

        public static Author from(User user) {
            return user == null
                    ? new Author(null, "알수없음")
                    : new Author(user.getId(), user.getName());
        }
    }

    /**
     * 댓글 본문에서 실제 멤버 멘션만 강조하기 위한 사용자 정보
     */
    public record MentionedUser(Long userId, String username, String name) {
    }

    /**
     * 부모 댓글 기준으로 대댓글까지 포함한 댓글 트리 정보를 생성
     */
    public static CommentInfo fromTree(SessionComment comment,
                                       Map<Long, List<SessionComment>> childrenByParent,
                                       Map<Long, List<MentionedUser>> mentionsByCommentId) {
        List<CommentInfo> replies = childrenByParent
                .getOrDefault(comment.getId(), List.of())
                .stream()
                .map(child -> fromTree(child, childrenByParent, mentionsByCommentId))
                .toList();

        // 삭제된 댓글은 본문을 숨기므로 멘션 정보도 함께 숨김
        List<MentionedUser> mentions = comment.isDeleted()
                ? List.of()
                : mentionsByCommentId.getOrDefault(comment.getId(), List.of());

        return new CommentInfo(
                comment.getId(),
                Author.from(comment.getUser()),
                comment.isDeleted() ? DELETED_COMMENT_MESSAGE : comment.getContent(),
                mentions,
                comment.isEdited(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                replies);
    }

    /**
     * 댓글 작성과 수정 직후 응답할 단일 댓글 정보를 생성
     */
    public static CommentInfo singleNonTree(SessionComment comment) {
        return new CommentInfo(
                comment.getId(),
                Author.from(comment.getUser()),
                comment.isDeleted() ? DELETED_COMMENT_MESSAGE : comment.getContent(),
                List.of(),
                comment.isEdited(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                List.of());
    }
}