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
        @Index(name = "idx_focus_session_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_focus_session_shared_crew_at", columnList = "shared_crew_id, shared_at DESC")
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

    @Column(name = "shared_crew_id")
    private Long sharedCrewId;

    @Column(name = "shared_at")
    private LocalDateTime sharedAt;

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
        if (this.status != SessionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 세션만 종료할 수 있습니다. 현재 상태: " + this.status);
        }
        if (endTime.isBefore(this.startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }

        this.endTime = endTime;
        this.status = SessionStatus.COMPLETED;
        this.durationSeconds = Duration.between(startTime, endTime).getSeconds();
    }

    public void softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 세션입니다.");
        }

        this.deletedAt = LocalDateTime.now();
    }

    public boolean isShared() {
        return this.sharedCrewId != null;
    }

    public boolean isSharedTo(Long crewId) {
        return crewId != null && crewId.equals(this.sharedCrewId);
    }

    public boolean isShareable() {
        if (this.status != SessionStatus.COMPLETED) return false;
        if (this.focusLog == null) return false;
        String content = this.focusLog.getContent();
        if (content == null || content.isBlank()) return false;
        return !"{}".equals(content.trim());
    }

    public void shareTo(Long crewId, LocalDateTime now) {
        this.sharedCrewId = crewId;
        this.sharedAt = now;
    }

    public void unshare() {
        this.sharedCrewId = null;
        this.sharedAt = null;
    }
}
