# Infrastructure

이 문서는 Application Port가 어떤 Infra 구현으로 연결되는지 설명합니다.

## 저장소 구현

Application 계층은 `com.deepflow.application.port.out.persistence`의 포트에 의존합니다. Infra 계층은 JPA 기반 구현체를 제공합니다.

| Port | Infra 구현 | Spring Data repository |
| --- | --- | --- |
| `UserRepository` | `UserRepositoryImpl` | `UserJpaRepository` |
| `SessionRepository` | `SessionRepositoryImpl` | `SessionJpaRepository` |
| `FocusLogRepository` | `FocusLogRepositoryImpl` | `FocusLogJpaRepository` |
| `CrewRepository` | `CrewRepositoryImpl` | `CrewJpaRepository` |
| `CrewMemberRepository` | `CrewMemberRepositoryImpl` | `CrewMemberJpaRepository` |
| `StatsRepository` | `StatsRepositoryImpl` | `StatsJpaRepository` |
| `AchievementRepository` | `AchievementRepositoryImpl` | `AchievementJpaRepository` |
| `UserAchievementRepository` | `UserAchievementRepositoryImpl` | `UserAchievementJpaRepository` |
| `SessionCommentRepository` | `SessionCommentRepositoryImpl` | `SessionCommentJpaRepository` |
| `CommentMentionRepository` | `CommentMentionRepositoryImpl` | `CommentMentionJpaRepository` |
| `SessionReactionRepository` | `SessionReactionRepositoryImpl` | `SessionReactionJpaRepository` |
| `SessionTagRepository` | `SessionTagRepositoryImpl` | `SessionTagJpaRepository` |
| `OutboxRepository` | `OutboxRepositoryImpl` | `OutboxJpaRepository` |

## MySQL

역할:
- 사용자, 세션, 로그, 크루, 댓글, 반응, 태그, 통계, 칭호, Outbox 저장
- 기본 검색 엔진이 `mysql`일 때 공유 세션 fulltext 검색

로컬 설정:
- host: `localhost`
- port: `3307`
- database: `demo`
- username/password: `root` / `root`

주의점:
- local은 `ddl-auto=update`
- prod는 `ddl-auto=validate`
- test는 `ddl-auto=create-drop`
- `FulltextIndexInitializer`와 MySQL fulltext 검색 SQL을 함께 봐야 검색 동작을 이해할 수 있음

## Redis

역할:
- Spring Cache 저장소
- Bucket4j rate limit 저장소
- Redisson 분산 락

관련 코드:
- `RedisConfig`
- `CacheConfig`
- `RateLimiterService`
- `DistributedLockAop`

주의점:
- 캐시 오류는 `CacheErrorHandler`에서 처리
- rate limit 오류는 fail-open
- 분산 락 Redis 오류는 `LOCK_ACQUISITION_FAILED`로 변환

## 검색

Application 포트:
- `SessionSearchPort`
- `SessionIndexer`

MySQL 검색:
- `MySqlFulltextSearchAdapter`
- 기본값
- `focus_session`, `focus_log`, `session_tag`를 조인해 검색
- 세션 검색은 제목, 요약, 태그를 함께 사용
- 태그 검색은 정규화된 태그 정확 일치 기준

Elasticsearch 검색:
- `ElasticsearchSearchAdapter`
- `SessionDocument`
- `SessionDocumentRepository`
- `ElasticsearchSessionIndexer`

색인 반영:
- 세션 공유, 공유 철회, 태그 변경 시 Outbox 이벤트 저장
- `OutboxProcessor`가 `SessionIndexer`로 색인 반영

주의점:
- `app.search.engine=es`일 때 검색 조회는 Elasticsearch를 사용
- `app.outbox.worker.enabled=false`면 색인 워커가 동작하지 않아 Elasticsearch 검색 데이터가 갱신되지 않을 수 있음
- 기본값은 MySQL 검색이라 Elasticsearch 없이도 로컬 검색 흐름은 동작 가능

## R2 이미지 저장

Application 포트:
- `ImageStorage`

Infra 구현:
- `R2ImageStorage`
- `R2Config`

설정:
- `app.r2.endpoint`
- `app.r2.access-key`
- `app.r2.secret-key`
- `app.r2.bucket`
- `app.r2.public-url`

흐름:
- 이미지 업로드는 `ImageService#upload`에서 `ImageStorage#upload`로 위임
- 로그 수정 시 제거된 이미지 URL은 `ImageService#deleteRemovedImages`가 삭제
- 세션 삭제나 대량 정리 정책은 별도로 확인 필요

## JWT

Application 포트:
- `TokenProvider`

Infra 구현:
- `JwtProvider`

설정:
- `jwt.secret`
- `jwt.access-token-validity-in-seconds`
- `jwt.refresh-token-validity-in-seconds`

흐름:
- 로그인: access token과 refresh token 발급
- refresh token은 사용자 엔티티에 저장
- API 요청: `JwtAuthenticationFilter`가 access token을 검증하고 `CustomUserDetails` 생성

## SSE

Application 포트:
- `AchievementNotifier`
- `AchievementStreamManager`
- `CommentNotificationNotifier`
- `CrewPresenceNotifier`

Infra 구현:
- `SseAchievementNotifier`
- `SseAchievementStreamManager`
- `SseCommentNotificationNotifier`
- `SseCrewPresenceNotifier`
- `SseEmitterManager`

채널:
- 칭호 달성
- 댓글/멘션 알림
- 크루 presence

주의점:
- SSE는 사용자와 채널 단위로 연결을 분리
- 같은 채널의 기존 연결은 새 연결 시 닫힘
- 댓글 알림은 연결된 사용자에게만 즉시 전송하고, 읽지 않은 알림 목록은 DB의 `CommentMention` 기준으로 조회

## 모니터링

Spring Actuator:
- `/actuator/health`
- `/actuator/prometheus`
- `/actuator/metrics`
- `/actuator/info`

Docker Compose:
- Prometheus: `9090`
- Grafana: `3001`

설정 파일:
- `monitoring/prometheus.yml`
- `monitoring/grafana/provisioning`
- `monitoring/grafana/dashboards`
