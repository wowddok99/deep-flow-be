# Architecture

## 모듈 의존성

```text
deep-flow-api
  ├── deep-flow-application
  └── deep-flow-infra

deep-flow-infra
  ├── deep-flow-application
  └── deep-flow-domain

deep-flow-application
  └── deep-flow-domain

deep-flow-domain
  └── 외부 모듈 의존 없음
```

## 계층 흐름

```text
API → Application → Domain
Application → Port
Infra → Port 구현
```

API는 HTTP 표현을 Application 유스케이스로 변환합니다. Application은 트랜잭션 안에서 도메인 객체와 포트를 조합합니다. Domain은 상태와 비즈니스 규칙을 표현합니다. Infra는 Application Port를 실제 기술로 구현합니다.

## deep-flow-api

담당:
- Spring Boot 진입점
- Controller
- Request/Response DTO
- 인증 필터와 Spring Security 설정
- CORS
- Rate Limit Interceptor 연결
- OpenAPI 설정
- 전역 예외 응답 변환

대표 코드:
- `DeepFlowApplication`
- `SecurityConfig`
- `JwtAuthenticationFilter`
- `RateLimitInterceptor`
- `GlobalExceptionHandler`
- `com.deepflow.api.controller`

API 계층은 도메인 규칙을 직접 처리하지 않고, 인증 사용자 ID와 요청 DTO를 Application 서비스 호출로 변환합니다.

## deep-flow-application

담당:
- 유스케이스 서비스
- 트랜잭션 경계
- 분산 락 진입점
- 저장소와 외부 시스템 포트 정의
- 도메인 이벤트 발행과 후처리
- DTO 조립

대표 코드:
- `SessionService`
- `CrewService`
- `SessionShareService`
- `SessionCommentService`
- `DailyFocusStatsService`
- `AchievementService`
- `OutboxPublisher`
- `OutboxProcessor`
- `com.deepflow.application.port.out`

Application 계층은 여러 도메인과 포트를 조합하는 곳입니다. 예를 들어 세션 종료는 `FocusSession#stop`으로 상태를 바꾸고, 커밋 이후 이벤트 리스너가 통계와 칭호 평가를 실행합니다.

## deep-flow-domain

담당:
- 도메인 엔티티
- 상태 변경 메서드
- enum
- 도메인 이벤트
- 공통 생성/수정 규칙

대표 코드:
- `User`
- `FocusSession`
- `FocusLog`
- `Crew`
- `CrewMember`
- `DailyFocusStats`
- `Achievement`
- `OutboxEvent`

Domain 계층은 저장소 구현, Redis, Elasticsearch, HTTP 같은 기술 세부사항을 알지 않습니다.

## deep-flow-infra

담당:
- JPA Repository 구현
- Redis 기반 Rate Limit
- Redisson 기반 분산 락
- Elasticsearch 또는 MySQL Fulltext 검색
- JWT 발급과 검증
- Cloudflare R2 이미지 저장
- SSE 알림 구현
- 캐시 설정

대표 코드:
- `*RepositoryImpl`
- `DistributedLockAop`
- `RateLimiterService`
- `ElasticsearchSearchAdapter`
- `MySqlFulltextSearchAdapter`
- `ElasticsearchSessionIndexer`
- `JwtProvider`
- `R2ImageStorage`
- `SseEmitterManager`

Infra 계층은 Application Port를 구현합니다. Application은 인터페이스에만 의존하므로 검색 엔진, 스토리지, 알림 방식 변경의 영향 범위를 Infra로 제한할 수 있습니다.

## 트랜잭션 경계

대부분의 쓰기 유스케이스는 Application 서비스 메서드의 `@Transactional`에서 시작합니다.

예:
- `SessionService#startSession`
- `SessionService#stopSession`
- `CrewService#create`
- `CrewService#joinCrewLockedInternal`
- `SessionShareService#shareLockedInternal`
- `SessionCommentService#create`

커밋 이후 후처리가 필요한 흐름은 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`를 사용합니다.

예:
- 세션 종료 후 통계 갱신과 칭호 평가
- 로그 수정 후 칭호 평가
- 댓글 저장 후 알림 발송
- 세션 공유 후 하이라이트 캐시 무효화

## 분산 락 경계

동시 요청으로 불변 조건이 깨질 수 있는 유스케이스는 분산 락을 사용합니다.

| 흐름 | 락 목적 | 대표 코드 |
| --- | --- | --- |
| 세션 시작 | 사용자별 진행 중 세션 중복 생성 방지 | `SessionService#startSession` |
| 크루 가입 | 크루 최대 인원 초과 방지 | `CrewJoinLocker`, `CrewService#joinCrewLockedInternal` |
| 초대 코드 발급 | 같은 크루 초대 코드 갱신 경쟁 방지 | `CrewService#issueInviteCode` |
| 세션 공유 | 같은 세션 중복 공유와 태그 갱신 경쟁 방지 | `SessionShareLocker`, `SessionShareService` |
| Outbox 워커 | 여러 워커의 같은 배치 중복 처리 방지 | `OutboxWorkerLocker` |

`CrewJoinLocker`와 `SessionShareLocker`는 락 획득 전 트랜잭션이 먼저 시작되는 문제를 피하기 위해 외부 진입점과 실제 트랜잭션 본문을 분리합니다.

## 캐시 경계

캐시는 읽기 성능을 위해 사용하지만, 쓰기 유스케이스에서 명시적으로 무효화합니다.

예:
- 세션 상세: `sessions`
- 시간대 분포: `hourlyDistribution`
- 크루 하이라이트: 공유, 공유 철회, 반응 변경 이벤트 후 무효화

캐시가 있는 기능을 수정할 때는 쓰기 경로에서 캐시 무효화가 필요한지 함께 확인해야 합니다.

## 외부 연동 경계

| 외부 기술 | 용도 | Port | Adapter |
| --- | --- | --- | --- |
| MySQL | 주요 영속성 | `*Repository` | `*RepositoryImpl`, `*JpaRepository` |
| Redis | 캐시, rate limit, Redisson 락 | `RateLimiter`, `DistributedLock` | `RateLimiterService`, `DistributedLockAop` |
| Elasticsearch | 공유 세션 검색 | `SessionSearchPort`, `SessionIndexer` | `ElasticsearchSearchAdapter`, `ElasticsearchSessionIndexer` |
| MySQL Fulltext | 검색 대체 구현 | `SessionSearchPort` | `MySqlFulltextSearchAdapter` |
| R2 | 이미지 저장 | `ImageStorage` | `R2ImageStorage` |
| SSE | 칭호, 댓글, 크루 presence 알림 | notifier ports | `Sse*Notifier`, `SseEmitterManager` |
| JWT | access/refresh token | `TokenProvider` | `JwtProvider` |

## 보안 경계

인증은 `SecurityConfig`와 `JwtAuthenticationFilter`에서 처리합니다. `/api/v1/auth/**`, Swagger, actuator를 제외한 API는 인증이 필요합니다.

Controller는 `@AuthenticationPrincipal CustomUserDetails`에서 사용자 ID를 가져와 Application 서비스에 전달합니다. 권한과 소유권 검증은 대부분 Application 서비스에서 도메인 상태와 저장소 조회를 조합해 수행합니다.

## Rate Limit 경계

`RateLimitInterceptor`는 요청 IP와 인증 사용자 기준으로 Bucket4j 버킷을 확인합니다.

특징:
- 익명 요청도 IP 기준으로 제한
- 로그인 사용자는 IP와 사용자 버킷을 함께 확인
- 세션 시작은 별도 비용 적용
- Redis 장애나 rate limit 검사 실패 시 fail-open 처리
- 초과 응답은 직접 JSON으로 작성해 `/error` 재진입으로 401이 되는 문제를 피함
