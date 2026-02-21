package com.deepflow.core.domain.log;

import com.deepflow.core.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_focus_log_image_log_order", columnList = "focus_log_id, orderIndex")
})
public class FocusLogImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "focus_log_id")
    private FocusLog focusLog;

    @Column(nullable = false)
    private String imageUrl;

    private int orderIndex;
}
