package com.deepflow.core.domain.log;

import com.deepflow.core.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FocusLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String title;
    
    private String summary;

    @Builder.Default
    @OneToMany(mappedBy = "focusLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FocusLogTag> logTags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "focusLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FocusLogImage> images = new ArrayList<>();

    public void update(String title, String content, String summary) {
        this.title = title;
        this.content = content;
        this.summary = summary;
    }
}
