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
@IdClass(SessionTagId.class)
@Table(
        name = "session_tag",
        indexes = {
                // PK 가 (session_id, tag) 라 session_id 단일 조회는 PK leftmost prefix 로 처리됨.
                // tag 단일 조회 (인기 태그/자동완성/필터) 는 별도 인덱스 필요.
                @Index(name = "idx_session_tag_tag", columnList = "tag")
        }
)
public class SessionTag extends BaseTimeEntity {

    @Id
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Id
    @Column(name = "tag", nullable = false, length = 30)
    private String tag;

    public static SessionTag of(Long sessionId, String normalizedTag) {
        return SessionTag.builder()
                .sessionId(sessionId)
                .tag(normalizedTag)
                .build();
    }
}
