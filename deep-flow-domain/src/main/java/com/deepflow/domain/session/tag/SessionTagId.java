package com.deepflow.domain.session.tag;

import java.io.Serializable;

/**
 * SessionTag 의 복합 PK. (session_id, tag) 조합 자체가 고유.
 * @IdClass 가 요구하는 형태: equals/hashCode 정의 + Serializable + 기본 생성자.
 * record 가 equals/hashCode/생성자 모두 자동 제공.
 */
public record SessionTagId(Long sessionId, String tag) implements Serializable {
    public SessionTagId() {
        this(null, null);
    }
}
