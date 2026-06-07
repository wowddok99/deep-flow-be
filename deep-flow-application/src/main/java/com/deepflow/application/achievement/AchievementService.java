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
        // 칭호 평가는 세션 흐름을 기준으로 수행되므로 평가 대상 세션을 조회
        FocusSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        // 칭호 조건 판정에 필요한 평가 컨텍스트 구성
        AchievementContext context = buildContext(userId, session, triggerType);

        // 현재 트리거 기준으로 지급 후보 칭호 코드를 수집
        List<String> candidateCodes = evaluators.stream()
                .filter(e -> e.supportedTriggers().contains(triggerType))
                .flatMap(e -> e.evaluate(context).stream())
                .distinct()
                .toList();

        // 지급 후보가 없으면 이후 처리 생략
        if (candidateCodes.isEmpty()) return List.of();

        // 지급 후보 코드를 실제 칭호 엔티티로 조회
        List<Achievement> achievements = achievementRepository.findByCodes(candidateCodes);

        // 칭호 획득 기록 생성을 위한 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 실제 수여에 성공한 칭호만 반환 대상으로 관리
        List<Achievement> grantedAchievements = new ArrayList<>();

        for (Achievement achievement : achievements) {
            try {
                // 사용자의 칭호 획득 기록 생성
                UserAchievement userAchievement = UserAchievement.create(user, achievement);
                userAchievementRepository.save(userAchievement);

                // 저장 성공한 칭호만 알림 대상으로 추가
                grantedAchievements.add(achievement);

                log.info("칭호 달성: userId={}, code={}, name={}, trigger={}",
                        userId, achievement.getCode(), achievement.getName(), triggerType);

            } catch (DataIntegrityViolationException e) {
                // 중복 수여는 DB 유니크 제약으로 방어하고 무시
                log.debug("Achievement already grantedAchievements (concurrent): userId={}, code={}",
                        userId, achievement.getCode());
            }
        }

        return grantedAchievements;
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
