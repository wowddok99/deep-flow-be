# Runtime Behavior

이 문서는 애플리케이션이 실행 중일 때 캐시, 비동기 작업, 스케줄러, SSE, 분산 락, rate limit이 어떻게 동작하는지 설명합니다.

## 비동기 실행

비동기 작업은 `AsyncConfig`의 `threadPoolTaskExecutor`를 사용합니다.

설정:
- `app.async.core-pool-size`
- `app.async.max-pool-size`
- `app.async.queue-capacity`

사용 지점:
- `SessionEventListener#handleSessionStoppedEvent`
- `SessionEventListener#handleLogUpdatedEvent`
- `CommentNotificationListener#on`
- `CrewPresenceService#onSessionStarted`
- `CrewPresenceService#onSessionStopped`

비동기 작업은 대부분 `@TransactionalEventListener(AFTER_COMMIT)`와 함께 사용됩니다. 원 트랜잭션이 커밋된 뒤 실행되어, 후처리가 조회하는 데이터가 이미 DB에 확정된 상태가 되도록 합니다.

## 스케줄러

### OutboxWorker

`OutboxWorker#run`은 1초마다 실행됩니다.

```text
@Scheduled(fixedDelay = 1000L)
→ OutboxWorkerLocker#runOnce
→ OutboxProcessor#processBatch
```

여러 인스턴스가 같은 Outbox 이벤트를 처리하지 않도록 `OutboxWorkerLocker`에 분산 락을 둡니다.

설정:
- `app.outbox.worker.enabled`
- `app.outbox.worker.batch-size`
- `app.outbox.worker.max-retry`

### SessionTimeScheduler

`SessionTimeScheduler`는 세션 시작 시 시간 기반 칭호 평가 작업을 예약합니다.

체크 지점:
- 10초
- 5분
- 15분
- 30분
- 1시간
- 2시간
- 3시간
- 4시간
- 5시간

세션 종료 시 `cancelForSession`으로 남은 예약을 취소합니다.

## 캐시

캐시 실패는 `CacheConfig`의 `CacheErrorHandler`가 처리합니다. Redis 연결 실패는 경고 로그로 남기고, 직렬화 오류나 기타 오류는 에러 로그로 남깁니다.

현재 캐시:

| Cache name | Key | 저장 지점 | 무효화 지점 |
| --- | --- | --- | --- |
| `sessions` | session id | `SessionService#getSessionDetail` | 로그 수정, 세션 종료, 삭제, 공유, 공유 철회 |
| `hourlyDistribution` | user id | `StatsDashboardService#getHourlyDistribution` | 세션 종료 |
| `crewHighlight` | crew id | `CrewHighlightCacheLoader#load` | 공유, 공유 철회, 반응 추가, 반응 제거 |

주의점:
- 캐시가 붙은 조회를 수정하면 관련 쓰기 경로의 무효화도 함께 확인해야 함
- `CrewHighlightService`는 권한 확인 후 캐시 프록시를 태우기 위해 `CrewHighlightCacheLoader`를 별도 빈으로 분리
- 캐시 장애는 핵심 유스케이스 실패보다 로그를 남기는 방향으로 설계됨

## SSE 채널

SSE 연결은 `SseEmitterManager`가 사용자 ID와 채널을 조합한 key로 관리합니다.

채널:
- `ACHIEVEMENT`
- `CREW_PRESENCE`
- `COMMENT_NOTIFICATION`

특징:
- 같은 사용자가 여러 채널을 동시에 유지할 수 있음
- 같은 사용자와 같은 채널의 기존 연결은 새 연결 시 닫힘
- timeout은 30분
- 연결 직후 `connect` 이벤트를 보냄

이벤트 이름:

| 이벤트 | 채널 | 발신 구현 |
| --- | --- | --- |
| `achievement` | `ACHIEVEMENT` | `SseAchievementNotifier` |
| `crew-presence` | `CREW_PRESENCE` | `SseCrewPresenceNotifier` |
| `comment-notification` | `COMMENT_NOTIFICATION` | `SseCommentNotificationNotifier` |

SSE 인증:
- `JwtAuthenticationFilter`는 `Authorization: Bearer` 외에 `token` query parameter도 허용
- EventSource가 커스텀 헤더를 보내기 어려운 제약 때문

## 분산 락

분산 락은 `@DistributedLock` 애노테이션과 `DistributedLockAop`가 처리합니다.

동작:

```text
@DistributedLock key SpEL 평가
→ Redisson lock 획득 시도
→ 락 안에서 AopForTransaction#proceed
→ 비즈니스 메서드 실행과 트랜잭션 커밋
→ 락 해제
```

락을 사용하는 이유:
- 진행 중 세션 중복 생성 방지
- 크루 최대 인원 초과 방지
- 초대 코드 갱신 경쟁 방지
- 세션 공유와 태그 변경 경쟁 방지
- Outbox worker 중복 실행 방지

주의점:
- 락 획득 실패는 `LOCK_ACQUISITION_FAILED`로 응답
- 락 안에서 트랜잭션 커밋까지 끝내야 락 해제 후 미커밋 상태가 노출되지 않음
- `CrewJoinLocker`, `SessionShareLocker`, `OutboxWorkerLocker`처럼 락 전용 진입점을 둔 이유가 여기에 있음

## Rate Limit

`RateLimitInterceptor`는 모든 HTTP 요청 전에 실행됩니다.

버킷:
- IP 기준 버킷
- 인증 사용자 기준 버킷
- 위반 누적 IP용 penalty 버킷

비용:
- 세션 시작: `app.rate-limit.session-start-cost`
- 쓰기 요청: `app.rate-limit.write-operation-cost`
- 읽기 요청: `app.rate-limit.read-operation-cost`

특징:
- 익명 요청도 IP 기준으로 제한
- 로그인 사용자는 IP와 사용자 기준을 함께 확인
- rate limit 검사 자체가 실패하면 fail-open
- 초과 시 `RATE_LIMIT_EXCEEDED`와 `X-Rate-Limit-Retry-After-Seconds` 헤더를 응답

## 프로필과 조건부 Bean

검색:
- `app.search.engine=mysql`: `MySqlFulltextSearchAdapter`
- `app.search.engine=es`: `ElasticsearchSearchAdapter`

Outbox:
- `app.outbox.worker.enabled=true`: `OutboxWorker`, `ElasticsearchSessionIndexer` 활성화

프로필:
- `local`: `ddl-auto=update`, SQL init always, rate limit 완화
- `prod`: `ddl-auto=validate`, secure cookie, prod CORS
- `test`: `ddl-auto=create-drop`, 테스트용 R2/JWT/rate limit 설정
