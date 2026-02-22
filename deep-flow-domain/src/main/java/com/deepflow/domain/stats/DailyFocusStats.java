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

    @Column(nullable = false)
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
}
