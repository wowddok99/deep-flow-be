package com.deepflow.domain.stats;

import com.deepflow.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_focus_stats_user_date", columnNames = {"userId", "date"})
})
public class DailyFocusStats extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 통계 적재는 쓰기 빈도가 높아 User 연관관계 대신 userId 만 저장
     *
     * - @ManyToOne 연관관계를 제거하여 쓰기 성능 최적화 및 시스템 결합도 감소
     * - 이벤트 기반 비동기 적재를 위해 FK 없이 userId만 직접 저장
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int totalSessions;

    @Column(nullable = false)
    private long totalDurationSeconds;

    public void addSession(long durationSeconds) {
        this.totalSessions++;
        this.totalDurationSeconds += durationSeconds;
    }

    public void addDuration(long durationSeconds) {
        this.totalDurationSeconds += durationSeconds;
    }
}
