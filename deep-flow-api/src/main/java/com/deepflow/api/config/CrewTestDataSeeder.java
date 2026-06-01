package com.deepflow.api.config;

import com.deepflow.application.port.out.persistence.CommentMentionRepository;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.CrewRepository;
import com.deepflow.application.port.out.persistence.SessionCommentRepository;
import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.domain.crew.Crew;
import com.deepflow.domain.crew.CrewMember;
import com.deepflow.domain.crew.Visibility;
import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import com.deepflow.domain.session.comment.CommentMention;
import com.deepflow.domain.session.comment.SessionComment;
import com.deepflow.domain.session.reaction.ReactionEmoji;
import com.deepflow.domain.session.reaction.SessionReaction;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * local 환경에서 크루 화면과 API 확인에 필요한 테스트 데이터 적재
 *
 * TestDataSeeder 이후 실행되어 기본 사용자와 함께 크루, 공유 세션,
 * 댓글, 멘션, 리액션, 진행 중 세션 시나리오 구성
 */
@Slf4j
@Component
@Profile("local")
@Order(2)
@RequiredArgsConstructor
public class CrewTestDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final SessionRepository sessionRepository;
    private final SessionTagRepository sessionTagRepository;
    private final SessionReactionRepository sessionReactionRepository;
    private final SessionCommentRepository sessionCommentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String COMMON_PASSWORD = "test1234";
    private static final Random RANDOM = new Random(42);

    private static final String[] POPULAR_TAGS = {
            "spring", "jpa", "algorithm", "react", "typescript",
            "docker", "kubernetes", "css", "system-design", "devops"
    };

    private static final ReactionEmoji[] EMOJIS = ReactionEmoji.values();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByUsername("testuser").isEmpty()) {
            log.warn("[CrewTestDataSeeder] testuser 가 없습니다. TestDataSeeder 가 먼저 실행되어야 합니다. 스킵합니다.");
            return;
        }
        if (userRepository.findByUsername("alice").isPresent()) {
            log.info("[CrewTestDataSeeder] 이미 시드된 상태입니다. 스킵합니다.");
            return;
        }

        log.info("[CrewTestDataSeeder] 크루 테스트 데이터 적재 시작");

        User testuser = userRepository.findByUsername("testuser").orElseThrow();
        User alice = createUser("alice", "김알리");
        User bob = createUser("bob", "박밥");
        User charlie = createUser("charlie", "최찰리");
        User dave = createUser("dave", "이데이브");
        User eve = createUser("eve", "한이브");

        // 공유 수에 따른 하이라이트 모드 확인용 시나리오 구성
        Crew startFresh = createCrew("StartFresh", "이제 막 시작한 크루. 첫 공유를 기다리고 있어요.",
                testuser.getId(), Visibility.PUBLIC, 20);
        addMember(startFresh.getId(), testuser.getId(), true);
        addMember(startFresh.getId(), alice.getId(), false);
        addMember(startFresh.getId(), bob.getId(), false);

        Crew studyClub = createCrew("StudyClub", "성장 중인 학습 크루. 함께 공부해요.",
                testuser.getId(), Visibility.PUBLIC, 20);
        addMember(studyClub.getId(), testuser.getId(), true);
        addMember(studyClub.getId(), alice.getId(), false);
        addMember(studyClub.getId(), bob.getId(), false);
        addMember(studyClub.getId(), charlie.getId(), false);

        Crew devElite = createCrew("DevElite", "활발한 개발자 크루. 매일 공유와 토론이 일어나요.",
                testuser.getId(), Visibility.PUBLIC, 50);
        addMember(devElite.getId(), testuser.getId(), true);
        addMember(devElite.getId(), alice.getId(), false);
        addMember(devElite.getId(), bob.getId(), false);
        addMember(devElite.getId(), charlie.getId(), false);
        addMember(devElite.getId(), dave.getId(), false);
        addMember(devElite.getId(), eve.getId(), false);

        // 비공개 크루 접근 제어 확인을 위해 testuser 는 멤버에서 제외
        Crew secretRoom = createCrew("SecretRoom", "비공개 학습 그룹.",
                alice.getId(), Visibility.PRIVATE, 5);
        addMember(secretRoom.getId(), alice.getId(), true);
        addMember(secretRoom.getId(), bob.getId(), false);
        secretRoom.issueInviteCode("INVITE7K", LocalDateTime.now().plusDays(1));
        crewRepository.save(secretRoom);

        log.info("[CrewTestDataSeeder] 크루 4개 적재: StartFresh({}), StudyClub({}), DevElite({}), SecretRoom({})",
                startFresh.getId(), studyClub.getId(), devElite.getId(), secretRoom.getId());

        List<User> studyClubUsers = List.of(testuser, alice, bob, charlie);
        List<FocusSession> growingSessions = new ArrayList<>();
        growingSessions.add(seedSharedSession(alice, studyClub.getId(), 6, "JPA N+1 학습",
                "오늘 Eager fetching 함정을 깨달았다. fetch join 도 같이 알아둬야 한다.",
                List.of("jpa", "spring"), 90));
        growingSessions.add(seedSharedSession(bob, studyClub.getId(), 5, "알고리즘 트리 DP",
                "구간 트리 응용 문제 풀이. 점화식 도출이 핵심.",
                List.of("algorithm", "tree-dp"), 120));
        growingSessions.add(seedSharedSession(charlie, studyClub.getId(), 3, "React Query 패턴",
                "낙관적 업데이트와 invalidate 의 균형을 어떻게 잡을지 정리.",
                List.of("react", "typescript"), 60));
        growingSessions.add(seedSharedSession(testuser, studyClub.getId(), 2, "Spring Security 6 변경점",
                "SecurityFilterChain 으로 마이그레이션하면서 부딪힌 점들.",
                List.of("spring", "security"), 75));
        growingSessions.add(seedSharedSession(alice, studyClub.getId(), 1, "Docker Compose 멀티 스테이지",
                "빌드 캐시 활용으로 이미지 크기 70% 감소.",
                List.of("docker", "devops"), 45));

        List<User> eliteUsers = List.of(testuser, alice, bob, charlie, dave, eve);
        List<FocusSession> matureSessions = new ArrayList<>();
        for (int i = 0; i < 35; i++) {
            User author = eliteUsers.get(i % eliteUsers.size());
            int daysAgo = (i % 7);
            int hourOffset = (i * 3) % 22 + 1;
            String title = MATURE_TITLES[i % MATURE_TITLES.length];
            String summary = MATURE_SUMMARIES[i % MATURE_SUMMARIES.length];
            List<String> tags = pickTags(i);
            int durationMin = 30 + (i * 7) % 240;

            FocusSession s = seedSharedSession(author, devElite.getId(), daysAgo, hourOffset,
                    title, summary, tags, durationMin);
            matureSessions.add(s);
        }

        // 가장 반응이 많은 공유 세션이 안정적으로 생기도록 첫 세션에 리액션 집중
        applyReactionsAll(matureSessions.get(0), eliteUsers);
        for (int i = 1; i < matureSessions.size(); i++) {
            FocusSession s = matureSessions.get(i);
            int reactionUsers = RANDOM.nextInt(4);
            for (int u = 0; u < reactionUsers; u++) {
                User reactor = eliteUsers.get((i + u) % eliteUsers.size());
                ReactionEmoji emoji = EMOJIS[(i + u) % EMOJIS.length];
                if (sessionReactionRepository.find(s.getId(), reactor.getId(), emoji).isEmpty()) {
                    sessionReactionRepository.save(SessionReaction.of(s.getId(), reactor.getId(), emoji));
                }
            }
        }
        for (int i = 0; i < growingSessions.size(); i++) {
            FocusSession s = growingSessions.get(i);
            User reactor = studyClubUsers.get((i + 1) % studyClubUsers.size());
            sessionReactionRepository.save(SessionReaction.of(s.getId(), reactor.getId(), ReactionEmoji.FIRE));
        }

        FocusSession g0 = growingSessions.get(0);
        SessionComment parent = sessionCommentRepository.save(
                SessionComment.create(g0.getId(), null, charlie, "저도 이거 빠졌었어요. @testuser 같이 공부해요!"));
        commentMentionRepository.save(CommentMention.create(parent.getId(), testuser.getId()));

        SessionComment reply = sessionCommentRepository.save(
                SessionComment.create(g0.getId(), parent.getId(), alice, "좋아요! 다음 주에 같이 풀어봐요."));

        FocusSession g3 = growingSessions.get(3);
        SessionComment c2 = sessionCommentRepository.save(
                SessionComment.create(g3.getId(), null, alice, "@bob 너도 이거 봤어? 도움됐어"));
        commentMentionRepository.save(CommentMention.create(c2.getId(), bob.getId()));

        // 삭제된 댓글의 멘션 알림 처리 확인용 데이터
        FocusSession g1 = growingSessions.get(1);
        SessionComment deletedC = sessionCommentRepository.save(
                SessionComment.create(g1.getId(), null, dave, "@charlie 이건 어떻게 푼거야?"));
        commentMentionRepository.save(CommentMention.create(deletedC.getId(), charlie.getId()));
        deletedC.softDelete(LocalDateTime.now());
        sessionCommentRepository.save(deletedC);

        FocusSession m0 = matureSessions.get(0);
        sessionCommentRepository.save(SessionComment.create(m0.getId(), null, alice, "정리 깔끔하네요!"));
        sessionCommentRepository.save(SessionComment.create(m0.getId(), null, dave, "참고할게요 👍"));
        sessionCommentRepository.save(SessionComment.create(m0.getId(), null, eve, "오 이거 저도 막혔던 부분이에요"));

        // 라이브 프레즌스 확인용 진행 중 세션
        seedOngoingSession(alice, 45);
        seedOngoingSession(charlie, 12);

        // 시드 트랜잭션 안에서는 updatedAt 과 sharedAt 이 거의 동일해 edited 가 모두 false
        // edited=true 시각 검증은 UI 에서 작성자(예: alice) 로 로그인 후 본인 공유 세션의
        // 본문을 한번 수정하면 자동으로 발생

        log.info("[CrewTestDataSeeder] 크루 테스트 데이터 적재 완료.");
        log.info("  - 사용자: testuser, alice, bob, charlie, dave, eve (모두 password = {})", COMMON_PASSWORD);
        log.info("  - 크루: StartFresh(EMPTY 모드), StudyClub(GROWING), DevElite(MATURE 35건), SecretRoom(PRIVATE+초대코드 INVITE7K)");
        log.info("  - GROWING 공유 5건, MATURE 공유 35건, ONGOING 2건");
        log.info("  - testuser/bob 에게 unread 멘션 각 1건");
        log.info("  - charlie 에게 unread 멘션 1건 (단 댓글 soft-deleted — TASK-002 검증용)");
        log.info("  - edited=true 검증은 UI 에서 본문 한 번 수정 시 발생");
    }

    private User createUser(String username, String name) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(COMMON_PASSWORD))
                .name(name)
                .role(Role.USER)
                .build());
    }

    private Crew createCrew(String name, String description, Long ownerId,
                            Visibility visibility, Integer maxMembers) {
        return crewRepository.save(Crew.create(name, description, ownerId, visibility, maxMembers));
    }

    private void addMember(Long crewId, Long userId, boolean owner) {
        crewMemberRepository.save(owner
                ? CrewMember.newOwner(crewId, userId)
                : CrewMember.newMember(crewId, userId));
    }

    private FocusSession seedSharedSession(User author, Long crewId, int daysAgo,
                                            String title, String summary,
                                            List<String> tags, int durationMin) {
        return seedSharedSession(author, crewId, daysAgo, 20, title, summary, tags, durationMin);
    }

    private FocusSession seedSharedSession(User author, Long crewId, int daysAgo, int hourOfDay,
                                            String title, String summary,
                                            List<String> tags, int durationMin) {
        LocalDateTime start = LocalDateTime.now()
                .minusDays(daysAgo)
                .withHour(hourOfDay).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMinutes(durationMin);
        LocalDateTime sharedAt = end.plusMinutes(5);

        FocusSession session = FocusSession.builder()
                .startTime(start)
                .endTime(end)
                .durationSeconds(Duration.between(start, end).getSeconds())
                .status(SessionStatus.COMPLETED)
                .user(author)
                .focusLog(FocusLog.builder()
                        .title(title)
                        .content("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\""
                                + summary.replace("\"", "\\\"") + "\"}]}]}")
                        .summary(summary)
                        .build())
                .build();
        session.shareTo(crewId, sharedAt);
        FocusSession saved = sessionRepository.save(session);

        if (!tags.isEmpty()) {
            sessionTagRepository.replaceAll(saved.getId(), tags);
        }
        return saved;
    }

    private void seedOngoingSession(User user, int minutesAgo) {
        LocalDateTime start = LocalDateTime.now().minusMinutes(minutesAgo);
        FocusSession session = FocusSession.builder()
                .startTime(start)
                .durationSeconds(0L)
                .status(SessionStatus.ONGOING)
                .user(user)
                .focusLog(FocusLog.builder()
                        .content("{}")
                        .summary("")
                        .build())
                .build();
        sessionRepository.save(session);
    }

    private void applyReactionsAll(FocusSession session, List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            ReactionEmoji emoji = EMOJIS[i % EMOJIS.length];
            if (sessionReactionRepository.find(session.getId(), u.getId(), emoji).isEmpty()) {
                sessionReactionRepository.save(SessionReaction.of(session.getId(), u.getId(), emoji));
            }
        }
    }

    // 인기 태그 집계가 흔들리지 않도록 고정된 분포 사용
    private List<String> pickTags(int seed) {
        int n = 1 + (seed % 3);
        List<String> pick = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String t = POPULAR_TAGS[(seed * 7 + i * 3) % POPULAR_TAGS.length];
            if (!pick.contains(t)) pick.add(t);
        }
        return pick;
    }

    private static final String[] MATURE_TITLES = {
            "Spring Boot 3.5 마이그레이션 정리",
            "JPA 동시성 제어 — 비관적 락 vs 낙관적 락",
            "알고리즘 — 세그먼트 트리 lazy propagation",
            "React 18 의 useTransition 패턴",
            "TypeScript 5 의 const type parameter",
            "Docker BuildKit 캐시 마운트 활용",
            "Kubernetes Probe 종류와 차이점",
            "CSS Grid subgrid 실전 적용",
            "시스템 디자인 — 채팅 서비스 설계",
            "GitHub Actions matrix 빌드 최적화",
            "PostgreSQL 인덱스 전략 정리",
            "Redis 분산락 구현 시 주의점",
            "GraphQL DataLoader 패턴",
            "Tailwind CSS 디자인 토큰 통합",
            "OAuth 2.1 변경점과 마이그레이션",
            "Elasticsearch 한국어 분석기 (nori) 튜닝",
            "MSA — Saga 패턴 vs 2PC",
            "Kafka 컨슈머 그룹 리밸런싱 동작",
            "WebSocket vs SSE 선택 기준",
            "Webpack Module Federation 도입기"
    };

    private static final String[] MATURE_SUMMARIES = {
            "공식 문서 + 마이그레이션 가이드 정리. 주요 breaking change 5가지 확인.",
            "동시성 시나리오별 락 선택 기준. 데드락 회피 패턴.",
            "lazy propagation 의 핵심 아이디어. 시간 복잡도 분석.",
            "useTransition 으로 무거운 렌더링을 양보하는 방법.",
            "const type parameter 로 타입 추론 정확도 ↑.",
            "RUN --mount=type=cache 로 빌드 시간 60% 감소.",
            "liveness/readiness/startup 의 차이와 운영 시 흔한 함정.",
            "subgrid 가 해결하는 문제와 브라우저 호환성.",
            "fan-out 모델 vs pull 모델 의 트레이드오프.",
            "matrix 와 strategy 의 효과적 조합.",
            "B-tree 와 BRIN 인덱스의 적합 시나리오.",
            "Redlock 알고리즘의 한계와 대안.",
            "N+1 회피의 우아한 패턴.",
            "토큰 일관성과 디자이너-개발자 워크플로우.",
            "PKCE 필수화의 의미와 적용 방법.",
            "사용자 사전과 동의어 사전의 운영.",
            "Saga 의 보상 트랜잭션 설계.",
            "리밸런싱 폭풍 회피 전략.",
            "단방향 vs 양방향 통신의 적합 시나리오.",
            "마이크로 프론트엔드 점진 도입 경험."
    };
}
