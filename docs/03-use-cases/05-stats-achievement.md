# Stats and Achievement Use Cases

## 통계 조회

### 목적

사용자의 일별, 주간, 대시보드, 요일별, 시간대별 집중 통계를 조회합니다.

### API

- `GET /api/v1/stats/overview`
- `GET /api/v1/stats/weekly`
- `GET /api/v1/stats/dashboard`
- `GET /api/v1/stats/weekly-trend`
- `GET /api/v1/stats/day-of-week`
- `GET /api/v1/stats/hourly`
- `GET /api/v1/stats/calendar`
- `GET /api/v1/stats/activity`
- `GET /api/v1/stats/all`

### Application

- `DailyFocusStatsService`
- `StatsDashboardService`

### 주의점

- 통계 원천은 세션 종료 후 비동기로 갱신되는 `DailyFocusStats`
- 진행 중 세션은 아직 일별 통계에 반영되지 않았을 수 있음
- 크루 활동 조회에서는 진행 중 세션 사용자를 오늘 활동자로 보정

### 관련 코드

- API: `StatsController`
- Application: `DailyFocusStatsService`, `StatsDashboardService`
- Domain: `DailyFocusStats`
- Persistence: `StatsRepository`, `SessionRepository`, `CrewMemberRepository`

### 갱신 기준

- 일별, 주간, 대시보드 집계 기준이 바뀌는 경우
- 세션 종료 후 통계 갱신 방식이 바뀌는 경우
- 크루 활동 조회의 보정 로직이 바뀌는 경우

## 칭호 달성

### 목적

세션 종료와 로그 수정 이벤트를 기준으로 칭호 조건을 평가하고 새 칭호를 지급합니다.

### 흐름 요약

```text
SessionStoppedEvent 또는 LogUpdatedEvent
→ SessionEventListener
→ AchievementService#checkAndGrant
→ AchievementEvaluator 구현체들
→ UserAchievementRepository#save
→ AchievementNotifier#notifyNewAchievements
```

### 주의점

- 트리거별로 지원하는 Evaluator만 실행
- 동시 지급 중복은 DB 유니크 제약과 예외 처리로 방어
- 새로 지급된 칭호만 SSE 알림 대상

### 관련 코드

- API: `AchievementController`
- Application: `AchievementService`, `SessionTimeScheduler`
- Domain: `Achievement`, `UserAchievement`, `AchievementCategory`
- Persistence: `AchievementRepository`, `UserAchievementRepository`, `UserRepository`, `SessionRepository`, `StatsRepository`
- Notification: `AchievementNotifier`

### 문서 반영 기준

- 칭호 조건이나 트리거가 바뀌는 경우
- 대표 칭호 설정 규칙이 바뀌는 경우
- 시간 기반 칭호 예약 시점이 바뀌는 경우
- 새 칭호 알림 채널이나 전송 방식이 바뀌는 경우
