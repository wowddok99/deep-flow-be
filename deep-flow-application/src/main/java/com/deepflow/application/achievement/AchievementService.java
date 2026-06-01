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
import org.springframework.dao.DataIntegrityViolationException;
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

    @Transactional
    public List<Achievement> checkAndGrant(Long userId, Long sessionId) {
        return checkAndGrant(userId, sessionId, TriggerType.SESSION_STOP);
    }

    /**
     * 현재 트리거에서 평가 가능한 칭호를 확인하고 새로 달성한 칭호만 수여
     *
     * @return 새로 수여된 칭호 목록 (SSE 알림용)
     */
    @Transactional
    public List<Achievement> checkAndGrant(Long userId, Long sessionId, TriggerType triggerType) {
        FocusSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        AchievementContext context = buildContext(userId, session, triggerType);

        List<String> newCodes = evaluators.stream()
                .filter(e -> e.supportedTriggers().contains(triggerType))
                .flatMap(e -> e.evaluate(context).stream())
                .distinct()
                .toList();

        if (newCodes.isEmpty()) return List.of();

        List<Achievement> achievements = achievementRepository.findByCodes(newCodes);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Achievement> granted = new ArrayList<>();
        for (Achievement achievement : achievements) {
            try {
                UserAchievement ua = UserAchievement.create(user, achievement);
                userAchievementRepository.save(ua);
                granted.add(achievement);

                log.info("칭호 달성: userId={}, code={}, name={}, trigger={}",
                        userId, achievement.getCode(), achievement.getName(), triggerType);

            } catch (DataIntegrityViolationException e) {
                // 동시 평가로 이미 지급된 칭호는 유니크 제약에 맡기고 무시
                log.debug("Achievement already granted (concurrent): userId={}, code={}",
                        userId, achievement.getCode());
            }
        }

        return granted;
    }

    /**
     * 전체 칭호와 사용자 달성 여부를 함께 조회
     */
    public List<AchievementInfo> getAllAchievements(Long userId) {
        List<Achievement> all = achievementRepository.findAll();
        Set<String> achievedCodes = userAchievementRepository.findAchievedCodesByUserId(userId);

        return all.stream()
                .map(a -> AchievementInfo.from(a, achievedCodes.contains(a.getCode())))
                .toList();
    }

    /**
     * 사용자가 획득한 칭호 목록 조회
     */
    public List<UserAchievementInfo> getMyAchievements(Long userId) {
        return userAchievementRepository.findByUserIdWithAchievement(userId).stream()
                .map(UserAchievementInfo::from)
                .toList();
    }

    /**
     * 사용자가 획득한 칭호만 대표 칭호로 설정
     */
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

    private AchievementContext buildContext(Long userId, FocusSession session, TriggerType triggerType) {
        long totalDuration = statsRepository.sumTotalDurationByUserId(userId);
        long totalSessions = statsRepository.sumTotalSessionsByUserId(userId);
        int streak = calculateStreak(userId);

        LocalDate userCreatedDate = userRepository.findById(userId)
                .map(u -> u.getCreatedAt().toLocalDate())
                .orElse(LocalDate.now());

        Set<String> achievedCodes = userAchievementRepository.findAchievedCodesByUserId(userId);

        return new AchievementContext(userId, session, totalDuration, totalSessions,
                streak, userCreatedDate, achievedCodes, triggerType);
    }

    private int calculateStreak(Long userId) {
        LocalDate checkDate = LocalDate.now();
        int streak = 0;

        while (true) {
            boolean hasSession = statsRepository.findByUserIdAndDate(userId, checkDate)
                    .map(s -> s.getTotalDurationSeconds() > 0)
                    .orElse(false);

            if (!hasSession) break;

            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }
}
