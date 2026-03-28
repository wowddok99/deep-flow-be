package com.deepflow.application.achievement;

import com.deepflow.application.achievement.dto.AchievementInfo;
import com.deepflow.application.achievement.dto.UserAchievementInfo;
import com.deepflow.application.exception.ResourceNotFoundException;
import com.deepflow.application.port.out.persistence.*;
import com.deepflow.domain.achievement.Achievement;
import com.deepflow.domain.achievement.UserAchievement;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementService {

    private final List<AchievementEvaluator> evaluators;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final StatsRepository statsRepository;
    private final SessionRepository sessionRepository;

    /**
     * 세션 완료 시 호출됨. 12개 Evaluator를 실행하여 달성 조건을 체크하고, 신규 칭호를 부여함.
     */
    @Transactional
    public List<Achievement> checkAndGrant(Long userId, Long sessionId) {
        FocusSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        AchievementContext context = buildContext(userId, session);

        List<String> newCodes = evaluators.stream()
                .flatMap(e -> e.evaluate(context).stream())
                .distinct()
                .toList();

        if (newCodes.isEmpty()) return List.of();

        List<Achievement> achievements = achievementRepository.findByCodes(newCodes);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Achievement> granted = new ArrayList<>();
        for (Achievement achievement : achievements) {
            UserAchievement ua = UserAchievement.create(user, achievement);
            userAchievementRepository.save(ua);
            granted.add(achievement);
            log.info("칭호 달성: userId={}, code={}, name={}", userId, achievement.getCode(), achievement.getName());
        }

        return granted;
    }

    public List<AchievementInfo> getAllAchievements(Long userId) {
        List<Achievement> all = achievementRepository.findAll();
        Set<String> achievedCodes = userAchievementRepository.findAchievedCodesByUserId(userId);

        return all.stream()
                .map(a -> AchievementInfo.from(a, achievedCodes.contains(a.getCode())))
                .toList();
    }

    public List<UserAchievementInfo> getMyAchievements(Long userId) {
        return userAchievementRepository.findByUserIdWithAchievement(userId).stream()
                .map(UserAchievementInfo::from)
                .toList();
    }

    @Transactional
    public void updateDisplayAchievement(Long userId, String achievementCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Achievement achievement = achievementRepository.findByCode(achievementCode)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementCode));

        Set<String> achievedCodes = userAchievementRepository.findAchievedCodesByUserId(userId);
        if (!achievedCodes.contains(achievementCode)) {
            throw new IllegalArgumentException("Not achieved yet: " + achievementCode);
        }

        user.updateDisplayAchievement(achievement);
    }

    private AchievementContext buildContext(Long userId, FocusSession session) {
        long totalDuration = statsRepository.sumTotalDurationByUserId(userId);
        long totalSessions = statsRepository.sumTotalSessionsByUserId(userId);
        int streak = calculateStreak(userId);
        LocalDate userCreatedDate = userRepository.findById(userId)
                .map(u -> u.getCreatedAt().toLocalDate())
                .orElse(LocalDate.now());
        Set<String> achievedCodes = userAchievementRepository.findAchievedCodesByUserId(userId);

        return new AchievementContext(userId, session, totalDuration, totalSessions, streak, userCreatedDate, achievedCodes);
    }

    // 오늘부터 역순으로 DailyFocusStats를 탐색하여 연속 기록 일수를 계산함
    private int calculateStreak(Long userId) {
        LocalDate checkDate = LocalDate.now();
        int streak = 0;

        while (true) {
            boolean hasSession = statsRepository.findByUserIdAndDate(userId, checkDate)
                    .map(s -> s.getTotalSessions() > 0)
                    .orElse(false);
            if (!hasSession) break;
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }
}
