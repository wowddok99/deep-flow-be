package com.deepflow.core.domain.log;

import com.deepflow.core.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import com.deepflow.core.converter.JsonConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition = "LONGTEXT")
    private Map<String, Object> content;

    private String title;
    
    private String summary;



    @Builder.Default
    @OneToMany(mappedBy = "focusLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FocusLogImage> images = new ArrayList<>();

    public void update(String title, Map<String, Object> content, String summary, List<String> imageUrls) {
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
