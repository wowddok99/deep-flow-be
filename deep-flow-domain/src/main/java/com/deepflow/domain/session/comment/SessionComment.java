package com.deepflow.domain.session.comment;

import com.deepflow.domain.common.BaseTimeEntity;
import com.deepflow.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "session_comment",
        indexes = {
                @Index(name = "idx_session_comment_session_block", columnList = "session_id, block_id"),
                @Index(name = "idx_session_comment_parent", columnList = "parent_id"),
                @Index(name = "idx_session_comment_deleted_at", columnList = "deleted_at")
        }
)
public class SessionComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * 블록 단위 댓글 실험 진입 시만 사용. 현재 항상 NULL.
     */
    @Column(name = "block_id", length = 64)
    private String blockId;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean edited;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static SessionComment create(Long sessionId, Long parentId, User author, String content) {
        return SessionComment.builder()
                .sessionId(sessionId)
                .parentId(parentId)
                .user(author)
                .content(content)
                .edited(false)
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void edit(String newContent) {
        this.content = newContent;
        this.edited = true;
    }

    public void softDelete(LocalDateTime now) {
        this.deletedAt = now;
    }
}
