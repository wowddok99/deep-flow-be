package com.deepflow.api.config;

import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.StatsRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import com.deepflow.domain.stats.DailyFocusStats;
import com.deepflow.domain.user.Role;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * local 환경에서 통계 대시보드 확인에 필요한 테스트 데이터 적재
 *
 * 계정: testuser / test1234
 * 기간: 2026-02-02 ~ 오늘
 */
@Slf4j
@Component
@Profile("local")
@Order(1)
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final StatsRepository statsRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "test1234";

    // 같은 날짜 통계가 여러 세션으로 쪼개질 수 있어 중복 저장 방지
    private final Map<LocalDate, DailyFocusStats> statsCache = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByUsername(TEST_USERNAME).isPresent()) {
            log.info("[TestDataSeeder] 테스트 계정이 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("[TestDataSeeder] 테스트 데이터 적재를 시작합니다.");

        User user = userRepository.save(User.builder()
                .username(TEST_USERNAME)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .name("테스트유저")
                .role(Role.USER)
                .build());

        Long userId = user.getId();
        LocalDate today = LocalDate.now();

        seed(user, userId, "2026-02-02 10:00", "2026-02-02 10:45", "첫 세션 - 환경 세팅");
        seed(user, userId, "2026-02-03 14:00", "2026-02-03 15:00", "프로젝트 구조 파악");
        seed(user, userId, "2026-02-05 09:30", "2026-02-05 10:30", "기본 기능 학습");
        seed(user, userId, "2026-02-07 20:00", "2026-02-07 21:30", "튜토리얼 따라하기");
        seed(user, userId, "2026-02-09 11:00", "2026-02-09 12:00", "코드 리딩");

        seed(user, userId, "2026-02-10 09:00", "2026-02-10 11:00", "API 설계 초안");
        seed(user, userId, "2026-02-11 14:00", "2026-02-11 16:30", "엔티티 모델링");
        seed(user, userId, "2026-02-12 10:00", "2026-02-12 12:00", "Repository 구현");
        seed(user, userId, "2026-02-13 15:00", "2026-02-13 17:00", "Service 레이어 작업");
        seed(user, userId, "2026-02-14 09:00", "2026-02-14 11:30", "Controller 연결");
        seed(user, userId, "2026-02-15 13:00", "2026-02-15 15:00", "테스트 코드 작성");
        seed(user, userId, "2026-02-19 10:00", "2026-02-19 13:00", "인증 기능 구현");
        seed(user, userId, "2026-02-20 14:00", "2026-02-20 17:00", "JWT 토큰 처리");
        seed(user, userId, "2026-02-21 09:00", "2026-02-21 11:00", "권한 체계 설계");
        seed(user, userId, "2026-02-22 22:00", "2026-02-23 01:30", "야간 디버깅");
        seed(user, userId, "2026-02-24 10:00", "2026-02-24 12:30", "버그 수정");
        seed(user, userId, "2026-02-25 14:00", "2026-02-25 16:00", "코드 리뷰 반영");
        seed(user, userId, "2026-02-27 09:00", "2026-02-27 12:00", "리팩토링");
        seed(user, userId, "2026-02-28 15:00", "2026-02-28 17:30", "문서화 작업");

        seed(user, userId, "2026-03-01 09:00", "2026-03-01 12:00", "세션 기능 개발");
        seed(user, userId, "2026-03-01 14:00", "2026-03-01 16:00", "세션 API 테스트");
        seed(user, userId, "2026-03-02 10:00", "2026-03-02 13:00", "집중 로그 기능");
        seed(user, userId, "2026-03-03 09:00", "2026-03-03 11:30", "이미지 업로드");
        seed(user, userId, "2026-03-04 14:00", "2026-03-04 17:00", "S3 연동");
        seed(user, userId, "2026-03-05 10:00", "2026-03-05 12:00", "프론트 연동 작업");
        seed(user, userId, "2026-03-08 09:00", "2026-03-08 11:00", "통계 기능 설계");
        seed(user, userId, "2026-03-09 13:00", "2026-03-09 16:00", "일별 통계 구현");
        seed(user, userId, "2026-03-10 09:30", "2026-03-10 12:00", "주간 추이 API");

        seed(user, userId, "2026-03-12 09:00", "2026-03-12 12:30", "업적 시스템 설계");
        seed(user, userId, "2026-03-12 14:00", "2026-03-12 16:00", "업적 평가 로직");
        seed(user, userId, "2026-03-13 10:00", "2026-03-13 13:00", "스트릭 계산 구현");
        seed(user, userId, "2026-03-14 09:00", "2026-03-14 12:00", "히든 업적 로직");
        seed(user, userId, "2026-03-14 21:00", "2026-03-15 00:30", "야간 업적 테스트");
        seed(user, userId, "2026-03-15 14:00", "2026-03-15 17:00", "업적 알림 기능");
        seed(user, userId, "2026-03-16 10:00", "2026-03-16 12:00", "SSE 연동");
        seed(user, userId, "2026-03-17 22:30", "2026-03-18 03:00", "배포 준비 야간 작업");
        seed(user, userId, "2026-03-19 09:00", "2026-03-19 11:00", "배포 후 모니터링");
        seed(user, userId, "2026-03-20 14:00", "2026-03-20 16:30", "핫픽스 대응");

        seed(user, userId, "2026-03-22 10:00", "2026-03-22 13:00", "대시보드 UI 작업");
        seed(user, userId, "2026-03-23 09:00", "2026-03-23 11:30", "차트 컴포넌트 구현");
        seed(user, userId, "2026-03-24 14:00", "2026-03-24 17:00", "히트맵 개발");
        seed(user, userId, "2026-03-25 10:00", "2026-03-25 12:00", "시간대별 분포 차트");
        seed(user, userId, "2026-03-29 09:00", "2026-03-29 12:00", "자정 경계 분할 로직 개발");
        seed(user, userId, "2026-03-30 14:00", "2026-03-30 16:30", "분할 로직 테스트");
        seed(user, userId, "2026-03-31 10:00", "2026-03-31 12:30", "엣지케이스 처리");

        seed(user, userId, "2026-04-01 09:00", "2026-04-01 11:00", "통계 QA");
        seed(user, userId, "2026-04-02 14:00", "2026-04-02 16:00", "이슈 분석");
        seed(user, userId, "2026-04-03 10:00", "2026-04-03 13:00", "이슈 문서 작성");

        seed(user, userId, today.minusDays(6).atTime(22, 0), today.minusDays(5).atTime(3, 30), "스트릭 로직 분석");
        seed(user, userId, today.minusDays(5).atTime(14, 0), today.minusDays(5).atTime(16, 30), "스트릭 쿼리 수정");
        seed(user, userId, today.minusDays(4).atTime(20, 0), today.minusDays(3).atTime(2, 0), "시간대별 분포 리팩토링");
        seed(user, userId, today.minusDays(3).atTime(10, 0), today.minusDays(3).atTime(11, 30), "코드 리뷰");
        seed(user, userId, today.minusDays(2).atTime(9, 0), today.minusDays(2).atTime(10, 0), "변화율 로직 수정");
        seed(user, userId, today.minusDays(1).atTime(21, 0), today.atTime(4, 0), "최종 검증 야간 작업");

        log.info("[TestDataSeeder] 테스트 데이터 적재 완료. 계정: {} / {}", TEST_USERNAME, TEST_PASSWORD);
        log.info("[TestDataSeeder] 세션 {}개, 통계 날짜 {}일", sessionCount, statsCache.size());
    }

    private int sessionCount = 0;

    private void seed(User user, Long userId, String startStr, String endStr, String title) {
        LocalDateTime start = LocalDateTime.parse(startStr.replace(" ", "T"));
        LocalDateTime end = LocalDateTime.parse(endStr.replace(" ", "T"));
        seed(user, userId, start, end, title);
    }

    private void seed(User user, Long userId, LocalDateTime start, LocalDateTime end, String title) {
        FocusSession session = FocusSession.builder()
                .startTime(start)
                .endTime(end)
                .durationSeconds(Duration.between(start, end).getSeconds())
                .status(SessionStatus.COMPLETED)
                .user(user)
                .focusLog(FocusLog.builder()
                        .title(title)
                        .content("{}")
                        .summary(title + " 완료")
                        .build())
                .build();
        sessionRepository.save(session);
        sessionCount++;

        // 실제 통계 적재와 동일하게 자정 경계 세션은 날짜별로 분할
        LocalDateTime currentStart = start;
        boolean isFirstDay = true;

        while (currentStart.toLocalDate().isBefore(end.toLocalDate())) {
            LocalDateTime nextMidnight = currentStart.toLocalDate().plusDays(1).atStartOfDay();
            long seconds = Duration.between(currentStart, nextMidnight).getSeconds();
            if (seconds > 0) {
                upsertStats(userId, currentStart.toLocalDate(), isFirstDay ? 1 : 0, seconds);
            }
            currentStart = nextMidnight;
            isFirstDay = false;
        }

        long seconds = Duration.between(currentStart, end).getSeconds();
        if (seconds > 0) {
            upsertStats(userId, currentStart.toLocalDate(), isFirstDay ? 1 : 0, seconds);
        }
    }

    private void upsertStats(Long userId, LocalDate date, int sessionDelta, long durationSeconds) {
        DailyFocusStats stats = statsCache.get(date);

        if (stats != null) {
            if (sessionDelta > 0) {
                stats.addSession(durationSeconds);
            } else {
                stats.addDuration(durationSeconds);
            }
        } else {
            stats = statsRepository.save(DailyFocusStats.builder()
                    .userId(userId)
                    .date(date)
                    .totalSessions(sessionDelta)
                    .totalDurationSeconds(durationSeconds)
                    .build());
            statsCache.put(date, stats);
        }
    }
}
