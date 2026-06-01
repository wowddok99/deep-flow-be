# Data Flow

이 문서는 단순 CRUD가 아니라 트랜잭션, 이벤트, 캐시, 검색 색인, 알림, 락이 얽힌 흐름을 설명합니다.

## 인증과 토큰 검증

```text
AuthService#login
→ UserRepository#findByUsername
→ PasswordEncoder#matches
→ TokenProvider#createAccessToken
→ TokenProvider#createRefreshToken
→ User#updateRefreshToken
→ User#afterLogin
```

요청 인증:

```text
HTTP 요청
→ JwtAuthenticationFilter
→ Authorization Bearer 또는 token query parameter 확인
→ TokenProvider#parseAndValidate
→ access token 여부 확인
→ CustomUserDetails 생성
→ SecurityContext 저장
→ Controller @AuthenticationPrincipal
```

특징:
- 로그인 시 refresh token은 사용자 엔티티에 저장
- 토큰 재발급은 요청 refresh token과 저장된 refresh token이 일치해야 성공
- 로그아웃은 저장된 refresh token을 제거
- SSE `EventSource`는 커스텀 헤더를 보내기 어려워 `token` query parameter도 허용
- access token이 아닌 token으로 API 인증을 시도하면 인증 컨텍스트를 만들지 않음

## 세션 종료 후 통계와 칭호 갱신

```text
SessionService#stopSession
→ FocusSession#stop
→ SessionStoppedEvent 발행
→ 트랜잭션 커밋
→ SessionEventListener#handleSessionStoppedEvent
→ DailyFocusStatsService#upsertStats
→ AchievementService#checkAndGrant
→ AchievementNotifier#notifyNewAchievements
```

특징:
- 세션 종료 저장이 확정된 뒤 통계와 칭호를 처리
- 후처리는 `@Async`와 `@TransactionalEventListener(AFTER_COMMIT)`로 실행
- 통계나 칭호 처리 실패는 로그로 남기고 세션 종료 자체는 되돌리지 않음
- 자정을 넘긴 세션은 날짜별로 duration을 나눠 저장

## 세션 시작 후 시간 기반 칭호 예약

```text
SessionService#startSession
→ FocusSession#create
→ SessionRepository#save
→ SessionTimeScheduler#scheduleForSession
→ CHECK_POINTS 기준 예약 작업 등록
→ 예약 시점 도달
→ AchievementService#checkAndGrant(TIME_CHECK)
→ AchievementNotifier#notifyNewAchievements
```

세션 종료 시:

```text
SessionService#stopSession
→ SessionTimeScheduler#cancelForSession
→ 세션별 ScheduledFuture 취소
```

특징:
- 장시간 세션 칭호는 세션 종료까지 기다리지 않고 임계치 시점에 평가
- 예약 지점은 `SessionTimeScheduler.CHECK_POINTS`에 정의
- 세션 종료 시 남은 예약을 취소해 종료된 세션의 시간 체크가 계속 실행되지 않도록 함
- 예약 실행 실패는 로그로 남기고 사용자 세션 흐름과 분리

## 로그 수정 후 칭호 평가

```text
SessionService#updateLog
→ FocusLog#update
→ ImageService#deleteRemovedImages
→ LogUpdatedEvent 발행
→ 트랜잭션 커밋
→ SessionEventListener#handleLogUpdatedEvent
→ AchievementService#checkAndGrant(LOG_UPDATE)
```

특징:
- 로그 내용이나 이미지 기반 칭호를 커밋 이후 평가
- 세션 상세 캐시는 로그 수정 시 무효화
- 제거된 이미지는 스토리지에서도 삭제

## 크루 Presence 전파

세션 시작 또는 종료 이벤트:

```text
SessionService#startSession 또는 stopSession
→ SessionStartedEvent 또는 SessionStoppedEvent 발행
→ 트랜잭션 커밋
→ CrewPresenceService
→ 사용자가 함께 속한 크루 멤버십 조회
→ 크루별 현재 진행 중 멤버 수 계산
→ CrewPresenceNotifier#broadcastToUsers
→ SSE 전파
```

크루 페이지 진입 시 현재 상태 조회:

```text
CrewLivePresenceController#getLivePresence
→ CrewLivePresenceService#getLivePresence
→ 크루 멤버십 확인
→ 크루 멤버 ID 조회
→ SessionRepository#findOngoingSessionsByUserIds
→ 진행 중 멤버 스냅샷 응답
```

특징:
- 이벤트 전파는 세션 상태 변경 커밋 이후 실행해 조회 결과와 SSE 이벤트가 어긋나지 않도록 함
- SSE는 이후 변경사항을 푸시하고, live presence API는 페이지 진입 시점 스냅샷을 제공
- presence 조회와 전파 모두 크루 멤버십을 기준으로 접근 범위를 제한

## 세션 공유와 검색 색인

```text
SessionShareLocker#share
→ SessionShareService#shareLockedInternal
→ FocusSession#shareTo
→ SessionTagRepository#replaceAll
→ OutboxPublisher#publish(SESSION_SHARED)
→ SessionSharedEvent 발행
→ 트랜잭션 커밋
→ OutboxWorker
→ OutboxProcessor#processBatch
→ SessionIndexer#index
```

특징:
- 공유 저장과 Outbox 저장은 같은 트랜잭션에서 처리
- Elasticsearch 같은 외부 검색 반영은 트랜잭션 밖 워커가 처리
- 검색 색인 실패는 Outbox 재시도 대상으로 남음
- 공유 이벤트는 크루 하이라이트 캐시 무효화에도 사용

## Outbox 워커와 중복 처리 방지

```text
OutboxWorker#run
→ 1초 fixedDelay tick
→ OutboxWorkerLocker#runOnce
→ @DistributedLock(key = "'outbox_worker'", waitTime = 0)
→ OutboxProcessor#processBatch
→ OutboxRepository#findPending
→ 이벤트별 handler 실행
→ 성공 시 markSuccess
→ 실패 시 markFailure
```

특징:
- 여러 애플리케이션 인스턴스가 동시에 같은 pending 이벤트를 처리하지 않도록 워커 실행에 분산 락을 사용
- `waitTime = 0`이라 다른 워커가 처리 중이면 해당 tick은 건너뜀
- 실패 이벤트는 retry count를 증가시키고 최대 재시도 한도까지 pending 대상으로 남음
- `app.outbox.worker.enabled`가 false면 워커와 Elasticsearch indexer가 비활성화될 수 있음

## 공유 철회와 검색 색인 삭제

```text
SessionShareService#unshareLockedInternal
→ FocusSession#unshare
→ SessionTagRepository#deleteAllBySessionId
→ OutboxPublisher#publish(SESSION_UNSHARED)
→ SessionUnsharedEvent 발행
→ OutboxProcessor
→ SessionIndexer#delete
```

특징:
- 공유 철회 시 태그도 함께 제거
- 검색 인덱스에서는 해당 세션 문서를 삭제
- 하이라이트 캐시는 공유 철회 이벤트 후 무효화

## 태그 변경과 검색 재색인

```text
SessionShareService#updateTagsLockedInternal
→ TagNormalizer
→ SessionTagRepository#replaceAll
→ OutboxPublisher#publish(SESSION_TAGS_UPDATED)
→ OutboxProcessor
→ SessionIndexer#index
```

특징:
- 태그 변경도 검색 결과에 영향을 주므로 공유 이벤트와 같은 색인 경로를 사용
- 태그는 정규화, 중복 제거, 최대 개수 제한을 거침

## 공유 세션 검색

```text
CrewSearchController#search
→ SearchService#search
→ 검색어 최소 길이 확인
→ 크루 멤버십 확인
→ 검색 타입 파싱
→ tag 검색이면 TagNormalizer 적용
→ page size와 offset 보정
→ SessionSearchPort#search
```

검색 구현 선택:

```text
app.search.engine=mysql
→ MySqlFulltextSearchAdapter
→ focus_session, focus_log, session_tag 대상 MySQL fulltext 검색

app.search.engine=es
→ ElasticsearchSearchAdapter
→ session document 대상 Elasticsearch 검색
```

특징:
- 기본 검색 엔진은 `mysql`
- `session` 검색은 제목, 요약, 태그를 함께 검색
- `tag` 검색은 정규화된 태그 정확 일치 기준
- 크루 멤버만 해당 크루 공유 세션을 검색할 수 있음
- Elasticsearch 검색 결과는 Outbox 색인 반영 지연에 영향을 받을 수 있음

## 댓글 작성과 알림

```text
SessionCommentService#create
→ 공유 세션과 멤버십 확인
→ SessionComment 저장
→ CommentMention 저장
→ SessionCommentCreatedEvent 발행
→ 트랜잭션 커밋
→ CommentNotificationListener
→ CommentNotificationNotifier
→ SSE 알림
```

특징:
- 댓글 저장이 확정된 뒤 알림을 보내 알림 클릭 시 댓글이 존재하도록 보장
- 글 작성자에게 댓글 알림 발송
- 멘션 대상이 크루 멤버인 경우 멘션 알림 발송
- 같은 댓글에서 글 작성자 알림과 멘션 알림이 중복되지 않도록 처리

## 댓글 알림 조회와 읽음 처리

```text
NotificationController#getUnread
→ NotificationService#getUnread
→ CommentMentionRepository unread 조회
→ SessionComment와 FocusSession 조합
→ NotificationResponse 응답
```

읽음 처리:

```text
NotificationController#markRead 또는 markAllRead
→ NotificationService#markRead 또는 markAllRead
→ 알림 소유자 확인
→ CommentMention#markRead
```

특징:
- 댓글 알림의 저장 기준은 `CommentMention`
- 단건 읽음 처리는 해당 mention이 요청 사용자에게 속하는지 확인
- 전체 읽음은 사용자 기준 unread mention을 한 번에 갱신

## 반응 토글과 집계

```text
SessionReactionController#toggle
→ SessionReactionService#toggle
→ ReactionEmoji#fromUnicode
→ 공유 세션과 크루 멤버십 확인
→ 기존 반응 조회
→ 있으면 삭제, 없으면 저장
→ SessionReactionAddedEvent 또는 SessionReactionRemovedEvent 발행
→ 현재 이모지 count 응답
```

집계 조회:

```text
SessionReactionController#aggregate
→ SessionReactionService#aggregate
→ 공유 세션과 크루 멤버십 확인
→ 세션의 전체 반응 조회
→ 사용자 정보 일괄 로드
→ 이모지별 count, 내 반응 여부, 상위 반응자 조립
```

특징:
- 허용된 `ReactionEmoji`만 사용할 수 있음
- 반응 추가와 제거 이벤트는 크루 하이라이트 캐시 무효화 대상
- 집계는 이모지별 count와 현재 사용자의 반응 여부를 함께 반환

## 크루 가입과 최대 인원 보호

```text
CrewService#joinByCode 또는 joinPublic
→ CrewJoinLocker#join
→ 분산 락 획득
→ CrewService#joinCrewLockedInternal
→ 중복 가입 확인
→ 현재 멤버 수 확인
→ CrewMember 저장
→ 트랜잭션 커밋
→ 락 해제
```

특징:
- 외부 진입점은 `Propagation.NOT_SUPPORTED`로 트랜잭션을 열지 않음
- 락 안에서 실제 가입 트랜잭션을 시작
- 최대 인원 검증과 멤버 저장 사이의 경쟁 조건을 줄임
- 공개 크루 가입은 공개 여부도 락 안에서 확인

## 초대 코드 발급

```text
CrewController#issueInviteCode
→ CrewService#issueInviteCode
→ TTL 허용값 확인
→ 크루 조회
→ 멤버십 확인
→ InviteCodeGenerator#generate
→ Crew#issueInviteCode
→ CrewRepository#save
```

특징:
- TTL은 `5`, `30`, `60`, `1440`분만 허용
- 같은 크루의 초대 코드 갱신 경쟁을 막기 위해 크루별 분산 락 사용
- 초대 코드 유니크 제약 충돌 시 새 후보로 최대 5회 재시도
- 초대 코드 가입 시 만료 여부는 `Crew#isInviteCodeValid`로 확인

## Rate Limit

```text
HTTP 요청
→ RateLimitInterceptor
→ IP 버킷 확인
→ 인증 사용자면 사용자 버킷 추가 확인
→ Controller
```

특징:
- Redis 기반 Bucket4j 사용
- 세션 시작 요청은 비용이 더 큼
- 위반이 누적된 IP는 penalty bucket 사용
- rate limit 검사 자체가 실패하면 서비스 가용성을 위해 fail-open

## 크루 하이라이트 계산

```text
CrewHighlightController#getHighlight
→ CrewHighlightService#getHighlight
→ 크루 멤버십 확인
→ CrewHighlightCacheLoader#load
→ 최근 7일 공유 세션 수 조회
→ HighlightMode 결정
→ 모드별 하이라이트 item 조립
```

모드:

```text
recentCount == 0
→ EMPTY

0 < recentCount < MATURE_THRESHOLD
→ GROWING
→ 최근 공유 카드 최대 3개

recentCount >= MATURE_THRESHOLD
→ MATURE
→ hot 세션, longest 세션, 인기 태그
```

특징:
- 권한 확인과 캐시 로드를 분리해 Spring Cache 프록시가 적용되도록 별도 `CrewHighlightCacheLoader`를 사용
- `MATURE_THRESHOLD`는 30건
- 최근 기준은 `RECENT_WINDOW` 7일
- hot score는 반응 수를 공유 후 경과 시간으로 나눈 값
- 공유, 공유 철회, 반응 추가, 반응 제거 이벤트 후 캐시를 무효화

## 크루 하이라이트 캐시 무효화

```text
SessionSharedEvent
SessionUnsharedEvent
SessionReactionAddedEvent
SessionReactionRemovedEvent
→ CrewHighlightCacheEvictor
→ 크루 하이라이트 캐시 무효화
```

특징:
- 하이라이트는 공유 세션, 반응, 태그 등 집계성 데이터에 의존
- 쓰기 이벤트 이후 캐시를 비워 다음 조회에서 최신 데이터를 다시 계산

## 이미지 업로드와 로그 이미지 정리

업로드:

```text
ImageController#upload
→ ImageService#upload
→ ImageStorage#upload
→ R2ImageStorage
→ public URL 응답
```

로그 수정 시 정리:

```text
SessionService#updateLog
→ 기존 FocusLogImage URL 목록 추출
→ FocusLog#update
→ ImageService#deleteRemovedImages
→ 새 이미지 목록에 없는 기존 URL 삭제
→ ImageStorage#delete
```

특징:
- 이미지 업로드는 R2 설정에 의존
- 로그 수정은 DB의 이미지 목록과 외부 스토리지 정리를 함께 고려해야 함
- 이미지 삭제 실패 처리가 유스케이스 실패로 이어지는지 확인하려면 `ImageService`와 `R2ImageStorage`를 함께 봐야 함
