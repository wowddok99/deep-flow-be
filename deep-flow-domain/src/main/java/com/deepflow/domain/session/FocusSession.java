package com.deepflow.domain.session;

import com.deepflow.domain.common.BaseTimeEntity;
import com.deepflow.domain.user.User;
import com.deepflow.domain.log.FocusLog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Table(indexes = {
        @Index(name = "idx_focus_session_user_status", columnList = "user_id, status"),
        @Index(name = "idx_focus_session_user_id_desc", columnList = "user_id, id DESC"),
        @Index(name = "idx_focus_session_deleted_at", columnList = "deleted_at")
})
public class FocusSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "focus_log_id")
    private FocusLog focusLog;

    private LocalDateTime deletedAt;

    public static FocusSession create(LocalDateTime startTime, User user) {
        return FocusSession.builder()
            .startTime(startTime)
            .status(SessionStatus.ONGOING)
            .durationSeconds(0L)
            .user(user)
            .focusLog(FocusLog.builder()
                .content("{}")
                .summary("")
                .build())
            .build();
    }

    public void stop(LocalDateTime endTime) {
        this.endTime = endTime;
        this.status = SessionStatus.COMPLETED;
        this.durationSeconds = Duration.between(startTime, endTime).getSeconds();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
