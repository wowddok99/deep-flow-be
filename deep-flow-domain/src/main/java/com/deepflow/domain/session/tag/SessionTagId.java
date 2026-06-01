package com.deepflow.domain.session.tag;

import java.io.Serializable;

/**
 * SessionTag 의 복합 PK
 *
 * @IdClass 요구사항을 record 로 충족해 equals, hashCode, 생성자 중복 구현 방지
 */
public record SessionTagId(Long sessionId, String tag) implements Serializable {
    public SessionTagId() {
        this(null, null);
    }
}
