package com.deepflow.core.domain.session;

import com.deepflow.core.domain.common.BaseTimeEntity;
import com.deepflow.core.domain.user.User;
import com.deepflow.core.domain.log.FocusLog;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "focus_log_id")
    private FocusLog focusLog;



    public static FocusSession create(LocalDateTime startTime, User user) {
        return FocusSession.builder()
            .startTime(startTime)
            .status(SessionStatus.ONGOING)
            .durationSeconds(0L)
            .user(user)
            .focusLog(FocusLog.builder()
                .content(new HashMap<>())
                .summary("")
                .build())
            .build();
    }

    public void stop(LocalDateTime endTime) {
        this.endTime = endTime;
        this.status = SessionStatus.COMPLETED;
        this.durationSeconds = Duration.between(startTime, endTime).getSeconds();
    }
}
