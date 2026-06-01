# Session Use Cases

## 집중 세션 시작

### 목적

사용자가 집중을 시작하면 진행 중 세션 중복을 막고 새 세션을 생성합니다.

### 흐름 요약

```text
POST /api/v1/sessions/start
→ SessionController#start
→ SessionService#startSession
→ UserRepository#findById
→ SessionRepository#existsByUserIdAndStatus
→ FocusSession#create
→ SessionRepository#save
→ SessionTimeScheduler#scheduleForSession
→ SessionStartedEvent 발행
```

### Application

- `SessionService#startSession`
- `@DistributedLock(key = "'session_start:' + #userId")`

### Domain

- `FocusSession`
- `FocusLog`
- `SessionStatus`
- `SessionStartedEvent`

### 주의점

- 사용자별 진행 중 세션은 하나만 허용
- 중복 시작 방지는 분산 락과 저장소 조회를 함께 사용
- 세션 시작 후 시간 기반 칭호 평가 예약을 등록

### 관련 코드

- API: `SessionController`
- Application: `SessionService#startSession`
- Domain: `FocusSession`, `FocusLog`, `SessionStatus`, `SessionStartedEvent`
- Persistence: `SessionRepository`, `UserRepository`
- Scheduler: `SessionTimeScheduler`

### 갱신 기준

- 세션 생성 조건이 바뀌는 경우
- 진행 중 세션 중복 검증 방식이 바뀌는 경우
- 세션 시작 후 예약 작업이나 칭호 평가 흐름이 바뀌는 경우
- 트랜잭션 범위나 분산 락 범위가 바뀌는 경우

## 집중 세션 종료

### 목적

진행 중 세션을 종료하고 집중 시간을 확정합니다. 통계와 칭호 평가는 세션 종료 트랜잭션 커밋 이후 처리합니다.

### 흐름 요약

```text
POST /api/v1/sessions/{id}/stop
→ SessionController#stop
→ SessionService#stopSession
→ FocusSession#stop
→ SessionTimeScheduler#cancelForSession
→ SessionStoppedEvent 발행
→ AFTER_COMMIT SessionEventListener
→ DailyFocusStatsService#upsertStats
→ AchievementService#checkAndGrant
→ AchievementNotifier
```

### Application

- `SessionService#stopSession`
- `SessionEventListener#handleSessionStoppedEvent`
- `DailyFocusStatsService#upsertStats`
- `AchievementService#checkAndGrant`

### Domain

- `FocusSession`
- `DailyFocusStats`
- `Achievement`
- `UserAchievement`
- `SessionStoppedEvent`

### 주의점

- 통계와 칭호 평가는 비동기 AFTER_COMMIT 후처리라 세션 종료 응답과 분리됨
- 통계 갱신 실패나 칭호 평가 실패는 로그로 남기고 원 세션 종료를 되돌리지 않음
- 자정을 넘긴 세션은 일자별로 집중 시간을 나눠 통계에 반영

### 관련 코드

- API: `SessionController`
- Application: `SessionService#stopSession`
- Domain: `FocusSession`, `DailyFocusStats`, `SessionStoppedEvent`
- Persistence: `SessionRepository`
- Listener: `SessionEventListener`, `DailyFocusStatsService`, `AchievementService`

### 갱신 기준

- 세션 종료 조건이나 상태 전이 규칙이 바뀌는 경우
- 종료 후 통계 반영 방식이 바뀌는 경우
- 시간 기반 칭호 예약 취소 규칙이 바뀌는 경우
- AFTER_COMMIT 비동기 후처리 방식이 바뀌는 경우

## 집중 로그 수정

### 목적

세션의 제목, 본문, 요약, 이미지 목록을 수정하고 로그 기반 칭호 평가를 트리거합니다.

### 흐름 요약

```text
PUT /api/v1/sessions/{id}/log
→ SessionController#updateLog
→ SessionService#updateLog
→ FocusLog#update
→ ImageService#deleteRemovedImages
→ LogUpdatedEvent 발행
→ AFTER_COMMIT SessionEventListener
→ AchievementService#checkAndGrant(LOG_UPDATE)
```

### 주의점

- 기존 이미지 URL과 새 이미지 URL을 비교해 제거된 이미지만 스토리지에서 삭제
- 세션 상세 캐시는 로그 수정 시 무효화
- 로그 수정 후 즉시 반영되어야 하는 칭호는 `LOG_UPDATE` 트리거 Evaluator가 처리

### 관련 코드

- API: `SessionController`
- Application: `SessionService#updateLog`
- Domain: `FocusLog`, `LogUpdatedEvent`
- Persistence: `SessionRepository`, `FocusLogRepository`
- Storage: `ImageService`, `ImageStorage`
- Listener: `SessionEventListener`, `AchievementService`

### 갱신 기준

- 로그 필드나 이미지 처리 방식이 바뀌는 경우
- 로그 수정 시 칭호 평가 트리거가 바뀌는 경우
- 이미지 삭제 정책이 바뀌는 경우
- 세션 상세 캐시 무효화 조건이 바뀌는 경우

## 세션 공유

### 목적

사용자의 종료된 세션을 크루에 공유하고 태그를 저장합니다.

### 흐름 요약

```text
POST /api/v1/sessions/{sessionId}/share
→ SessionShareController#share
→ SessionShareLocker#share
→ SessionShareService#shareLockedInternal
→ SessionRepository#findByIdAndUserId
→ CrewMemberRepository#existsByCrewIdAndUserId
→ FocusSession#shareTo
→ SessionTagRepository#replaceAll
→ OutboxPublisher#publish(SESSION_SHARED)
→ SessionSharedEvent 발행
```

### 주의점

- 자기 세션만 공유 가능
- 이미 공유된 세션은 다시 공유할 수 없음
- 진행 중 세션처럼 공유 불가능한 상태는 차단
- 공유 대상 크루의 멤버여야 함
- 검색 색인은 Outbox로 비동기 반영
- 하이라이트 캐시는 공유 이벤트 후 무효화

### 관련 코드

- API: `SessionShareController`
- Application: `SessionShareLocker`, `SessionShareService`
- Domain: `FocusSession`, `SessionTag`, `SessionSharedEvent`, `SessionUnsharedEvent`
- Persistence: `SessionRepository`, `SessionTagRepository`, `CrewMemberRepository`
- Outbox: `OutboxPublisher`, `OutboxProcessor`, `SessionIndexer`

### 문서 반영 기준

- 세션 공유 가능 조건이 바뀌는 경우
- 공유 태그 개수나 정규화 규칙이 바뀌는 경우
- 공유 철회 시 처리 흐름이 바뀌는 경우
- 검색 색인이나 하이라이트 무효화 흐름이 바뀌는 경우
