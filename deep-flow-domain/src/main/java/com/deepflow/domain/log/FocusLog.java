package com.deepflow.domain.log;

import com.deepflow.domain.common.BaseTimeEntity;
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
    private List<FocusLogImage> images = new ArrayList<>();

    public void update(String title, String content, String summary, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.summary = summary;

        updateImages(imageUrls);
    }

    private void updateImages(List<String> newImageUrls) {
        this.images.clear();

        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            for (int i = 0; i < newImageUrls.size(); i++) {
                FocusLogImage image = FocusLogImage.builder()
                        .focusLog(this)
                        .imageUrl(newImageUrls.get(i))
                        .orderIndex(i)
                        .build();
                this.images.add(image);
            }
        }
    }
}
