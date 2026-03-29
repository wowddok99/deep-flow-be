package com.deepflow.domain.achievement;

import com.deepflow.domain.common.BaseTimeEntity;
import com.deepflow.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_achievement",
        columnNames = {"user_id", "achievement_id"}
    ),
    indexes = @Index(name = "idx_user_achievement_user", columnList = "user_id")
)
public class UserAchievement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    private LocalDateTime achievedAt;

    public static UserAchievement create(User user, Achievement achievement) {
        UserAchievement ua = new UserAchievement();
        ua.user = user;
        ua.achievement = achievement;
        ua.achievedAt = LocalDateTime.now();
        return ua;
    }
}
