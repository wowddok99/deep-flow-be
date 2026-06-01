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
                // 복합 기본키의 첫 컬럼이 session_id 라서 tag 단일 조회에는 별도 인덱스 필요
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
