package com.deepflow.domain.session.tag;

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
        name = "session_tag",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_session_tag", columnNames = {"session_id", "tag"})
        },
        indexes = {
                @Index(name = "idx_session_tag_session_id", columnList = "session_id"),
                @Index(name = "idx_session_tag_tag", columnList = "tag")
        }
)
public class SessionTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "tag", nullable = false, length = 30)
    private String tag;

    public static SessionTag of(Long sessionId, String normalizedTag) {
        return SessionTag.builder()
                .sessionId(sessionId)
                .tag(normalizedTag)
                .build();
    }
}
