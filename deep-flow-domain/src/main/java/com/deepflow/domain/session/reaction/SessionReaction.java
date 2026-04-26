package com.deepflow.domain.session.reaction;

import com.deepflow.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "session_reaction",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_session_reaction", columnNames = {"session_id", "user_id", "emoji"})
        },
        indexes = {
                @Index(name = "idx_session_reaction_session", columnList = "session_id")
        }
)
public class SessionReaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReactionEmoji emoji;

    public static SessionReaction of(Long sessionId, Long userId, ReactionEmoji emoji) {
        return SessionReaction.builder()
                .sessionId(sessionId)
                .userId(userId)
                .emoji(emoji)
                .build();
    }
}
