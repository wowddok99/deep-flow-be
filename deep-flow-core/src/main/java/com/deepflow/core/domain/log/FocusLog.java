package com.deepflow.core.domain.log;

import com.deepflow.core.domain.common.BaseTimeEntity;
import com.deepflow.core.domain.session.FocusSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "focus_log")
public class FocusLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String summary;

    private String tags;

    @OneToOne(mappedBy = "log")
    private FocusSession session;

    public FocusLog(String content, String summary, String tags) {
        this.content = content;
        this.summary = summary;
        this.tags = tags;
    }

    public void setSession(FocusSession session) {
        this.session = session;
    }

    public void update(String content, String summary, String tags) {
        this.content = content;
        this.summary = summary;
        this.tags = tags;
    }
}
