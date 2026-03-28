package com.deepflow.application.achievement.evaluator;

import com.deepflow.application.achievement.AchievementContext;
import com.deepflow.application.achievement.AchievementEvaluator;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.StatsRepository;
import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.FocusSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HiddenEvaluator implements AchievementEvaluator {

    private final SessionRepository sessionRepository;
    private final StatsRepository statsRepository;

    @Override
    public List<String> evaluate(AchievementContext context) {
        List<String> achieved = new ArrayList<>();
        FocusSession session = context.completedSession();
        FocusLog log = session.getFocusLog();

        // H-01: 자정의 경계인 - 23:50 이전 시작, 00:10 이후 종료
        if (!context.alreadyAchieved("H-01")) {
            int startHour = session.getStartTime().getHour();
            int startMin = session.getStartTime().getMinute();
            int endHour = session.getEndTime().getHour();
            int endMin = session.getEndTime().getMinute();
            boolean startedBefore = (startHour == 23 && startMin >= 40);
            boolean endedAfter = (endHour == 0 && endMin >= 10) || (endHour > 0 && endHour < 5);
            if (startedBefore && endedAfter) achieved.add("H-01");
        }

        // H-02: 불사조 - 7일 이상 미접속 후 복귀
        if (!context.alreadyAchieved("H-02")) {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            // 어제부터 역순으로 기록 없는 날 체크
            int gapDays = 0;
            for (int i = 1; i <= 30; i++) {
                LocalDate checkDate = today.minusDays(i);
                boolean hasSession = statsRepository.findByUserIdAndDate(context.userId(), checkDate)
                        .map(s -> s.getTotalSessions() > 0).orElse(false);
                if (!hasSession) {
                    gapDays++;
                } else {
                    break;
                }
            }
            if (gapDays >= 7) achieved.add("H-02");
        }

        // H-03: 완벽주의자 - 제목 + 본문 1000자 + 요약 + 이미지
        if (!context.alreadyAchieved("H-03")) {
            boolean hasTitle = log.getTitle() != null && !log.getTitle().isBlank();
            boolean hasContent = log.getContent() != null && log.getContent().length() >= 1000;
            boolean hasSummary = log.getSummary() != null && !log.getSummary().isBlank();
            boolean hasImages = log.getImages() != null && !log.getImages().isEmpty();
            if (hasTitle && hasContent && hasSummary && hasImages) achieved.add("H-03");
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

        // H-08: 기록 폭주 - 본문 5000자 이상
        if (!context.alreadyAchieved("H-08")) {
            if (log.getContent() != null && log.getContent().length() >= 5000) {
                achieved.add("H-08");
            }
        }

        return achieved;
    }
}
