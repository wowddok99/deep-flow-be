package com.deepflow.application.achievement;

import com.deepflow.domain.session.FocusSession;

import java.time.LocalDate;
import java.util.Set;

public record AchievementContext(
    Long userId,
    FocusSession completedSession,
    long totalDurationSeconds,
    long totalSessions,
    int currentStreak,
    LocalDate userCreatedDate,
    Set<String> achievedCodes,
    TriggerType triggerType
) {
    public boolean alreadyAchieved(String code) {
        return achievedCodes.contains(code);
    }
}
