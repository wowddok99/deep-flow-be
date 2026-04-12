package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.achievement.TriggerType;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.StatsRepository;
import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** 히든 칭호 평가 (자정 경계, 복귀, 완벽주의, 기록 폭주 등 특수 조건) */
@Component
@RequiredArgsConstructor
public class HiddenEvaluator implements AchievementEvaluator {

    private final SessionRepository sessionRepository;
    private final StatsRepository statsRepository;

    @Override
    public Set<TriggerType> supportedTriggers() {
        return Set.of(TriggerType.SESSION_STOP, TriggerType.TIME_CHECK, TriggerType.LOG_UPDATE);
    }

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        FocusSession session = context.completedSession();
        FocusLog log = session.getFocusLog();
        TriggerType trigger = context.triggerType();

        // H-01: 자정의 경계인 - 23:50 이전 시작, 현재(또는 종료) 시각이 00:10 이후
        // TIME_CHECK / SESSION_STOP 모두에서 감지
        if (!context.alreadyAchieved("H-01") && trigger != TriggerType.LOG_UPDATE) {
            int startHour = session.getStartTime().getHour();
            int startMin = session.getStartTime().getMinute();
            boolean startedBefore = (startHour == 23 && startMin >= 40);

            if (startedBefore) {
                LocalDateTime checkTime = session.getStatus() == SessionStatus.ONGOING
                        ? LocalDateTime.now()
                        : session.getEndTime();
                int endHour = checkTime.getHour();
                int endMin = checkTime.getMinute();
                boolean endedAfter = (endHour == 0 && endMin >= 10) || (endHour > 0 && endHour < 5);
                if (endedAfter) achieved.add("H-01");
            }
        }

        // H-03: 완벽주의자 - 제목 + 본문 1000자 + 요약 + 이미지
        // LOG_UPDATE / SESSION_STOP에서 감지 (현재 세션 객체 직접 참조)
        if (!context.alreadyAchieved("H-03") && trigger != TriggerType.TIME_CHECK) {
            boolean hasTitle = log.getTitle() != null && !log.getTitle().isBlank();
            boolean hasContent = log.getContent() != null && log.getContent().length() >= 1000;
            boolean hasSummary = log.getSummary() != null && !log.getSummary().isBlank();
            boolean hasImages = log.getImages() != null && !log.getImages().isEmpty();
            if (hasTitle && hasContent && hasSummary && hasImages) achieved.add("H-03");
        }

        // H-08: 기록 폭주 - 본문 5000자 이상
        // LOG_UPDATE / SESSION_STOP에서 감지 (현재 세션 객체 직접 참조)
        if (!context.alreadyAchieved("H-08") && trigger != TriggerType.TIME_CHECK) {
            if (log.getContent() != null && log.getContent().length() >= 5000) {
                achieved.add("H-08");
            }
        }

        // 아래 칭호들은 SESSION_STOP에서만 평가 (누적 쿼리/종료 시각 기반)
        if (trigger == TriggerType.SESSION_STOP) {
            // H-02: 불사조 - 7일 이상 미접속 후 복귀
            if (!context.alreadyAchieved("H-02")) {
                int gapDays = 0;
                for (int i = 1; i <= 30; i++) {
                    LocalDate checkDate = LocalDate.now().minusDays(i);
                    boolean hasSession = statsRepository.findByUserIdAndDate(context.userId(), checkDate)
                            .map(s -> s.getTotalDurationSeconds() > 0).orElse(false);
                    if (!hasSession) {
                        gapDays++;
                    } else {
                        break;
                    }
                }
                if (gapDays >= 7) achieved.add("H-02");
            }

            // H-04: 첫날의 열정 - 가입 당일 3세션 이상
            if (!context.alreadyAchieved("H-04")) {
                LocalDate joinDate = context.userCreatedDate();
                if (LocalDate.now().equals(joinDate)) {
                    var stats = statsRepository.findByUserIdAndDate(context.userId(), joinDate);
                    if (stats.map(s -> s.getTotalSessions() >= 3).orElse(false)) {
                        achieved.add("H-04");
                    }
                }
            }

            // H-05: 더블 마라톤 - 하루에 2시간 이상 세션 2회
            if (!context.alreadyAchieved("H-05")) {
                long count = sessionRepository.countByUserIdAndDateAndMinDuration(
                        context.userId(), LocalDate.now(), 7200L);
                if (count >= 2) achieved.add("H-05");
            }

            // H-06: 고요한 새벽 - 새벽 3~4시에 2시간 이상 세션
            if (!context.alreadyAchieved("H-06")) {
                int endHour = session.getEndTime().getHour();
                if (endHour >= 3 && endHour < 5 && session.getDurationSeconds() >= 7200) {
                    achieved.add("H-06");
                }
            }

            // H-07: 10의 법칙 - 세션 10회 + 10시간 + 로그 10개
            if (!context.alreadyAchieved("H-07")) {
                boolean sessions10 = context.totalSessions() >= 10;
                boolean hours10 = context.totalDurationSeconds() >= 36_000;
                long logs10 = sessionRepository.countByUserIdWithMinContentLength(context.userId(), 100);
                if (sessions10 && hours10 && logs10 >= 10) achieved.add("H-07");
            }
        }

        return achieved;
    }
}
