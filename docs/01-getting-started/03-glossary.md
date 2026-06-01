# Glossary

## User

서비스 사용자입니다. 인증 정보, 이름, 권한, refresh token, 대표 칭호를 가집니다.

대표 코드: `User`, `Role`

## FocusSession

사용자가 집중을 시작하고 종료하기까지의 한 단위입니다. 시작 시간, 종료 시간, 진행 상태, 집중 로그, 공유 크루 정보를 가집니다.

대표 코드: `FocusSession`, `SessionStatus`

## FocusLog

집중 세션에 작성하는 제목, 본문, 요약, 이미지 목록입니다. 세션 생성 시 함께 생성되고 이후 로그 수정 API로 갱신됩니다.

대표 코드: `FocusLog`, `FocusLogImage`

## Crew

사용자들이 집중 기록을 공유하는 그룹입니다. 공개 여부, 최대 멤버 수, 초대 코드, 소유자 정보를 가집니다.

대표 코드: `Crew`, `Visibility`

## CrewMember

사용자와 크루의 멤버십입니다. 소유자와 일반 멤버 역할을 구분합니다.

대표 코드: `CrewMember`, `CrewRole`

## Shared Session

크루에 공개된 집중 세션입니다. 종료된 세션만 공유할 수 있으며, 공유된 세션은 피드, 댓글, 반응, 검색 대상이 됩니다.

대표 코드: `SessionShareService`, `CrewFeedService`

## SessionTag

공유 세션에 붙는 태그입니다. 태그는 정규화되어 저장되고 크루 피드 필터와 검색 보조 정보로 사용됩니다.

대표 코드: `SessionTag`, `TagNormalizer`, `SessionTagRepository`

## SessionComment

공유 세션에 작성하는 댓글입니다. 부모 댓글 ID를 통해 대댓글을 표현하고, 삭제는 soft delete로 처리합니다.

대표 코드: `SessionComment`, `SessionCommentService`

## CommentMention

댓글에서 언급된 사용자를 나타냅니다. 댓글 알림과 읽음 처리의 기준이 됩니다.

대표 코드: `CommentMention`, `NotificationService`

## SessionReaction

공유 세션에 남기는 이모지 반응입니다. 사용자, 세션, 이모지 조합으로 중복을 제어합니다.

대표 코드: `SessionReaction`, `ReactionEmoji`

## DailyFocusStats

사용자별 날짜 단위 집중 통계입니다. 세션 수와 총 집중 시간을 저장하며 대시보드와 칭호 평가에 사용됩니다.

대표 코드: `DailyFocusStats`, `DailyFocusStatsService`, `StatsDashboardService`

## Achievement

사용자가 달성할 수 있는 칭호 정의입니다. 여러 Evaluator가 세션 종료나 로그 수정 이벤트를 기준으로 달성 여부를 평가합니다.

대표 코드: `Achievement`, `UserAchievement`, `AchievementService`, `AchievementEvaluator`

## Outbox

트랜잭션 안에서 후처리 이벤트를 저장하고, 별도 워커가 검색 색인 같은 외부 반영을 처리하기 위한 이벤트 저장소입니다.

대표 코드: `OutboxEvent`, `OutboxPublisher`, `OutboxProcessor`, `OutboxWorker`

## Port

Application 계층이 Infra 구현에 직접 의존하지 않기 위해 정의하는 인터페이스입니다. 저장소, 검색, 알림, 스토리지, 토큰, rate limit 같은 외부 경계를 표현합니다.

대표 패키지: `com.deepflow.application.port.out`

## Adapter

Application Port를 실제 기술로 구현한 Infra 클래스입니다. JPA, Redis, Elasticsearch, JWT, R2, SSE 구현이 여기에 해당합니다.

대표 패키지: `com.deepflow.infra`

## Distributed Lock

동시 요청에서 같은 자원에 대한 중복 생성이나 정원 초과를 막기 위한 분산 락입니다. 세션 시작, 크루 가입, 세션 공유처럼 경쟁 조건이 있는 유스케이스에 사용됩니다.

대표 코드: `@DistributedLock`, `DistributedLockAop`, `CrewJoinLocker`, `SessionShareLocker`

## CommonResponse

API 성공과 실패 응답을 감싸는 공통 응답 형식입니다. 성공 시 `success=true`, 실패 시 `success=false`와 `ApiError`를 사용합니다.

대표 코드: `CommonResponse`, `ApiError`, `GlobalExceptionHandler`

## ErrorCode

Application 계층의 비즈니스 예외가 API 응답 status와 error code로 변환될 때 사용하는 enum입니다.

대표 코드: `ErrorCode`, `CustomException`

## Cursor

목록 조회에서 다음 페이지 기준점을 나타내는 값입니다. 세션 목록은 ID 기반 cursor를 사용하고, 크루 피드는 `SharedFeedCursor`를 token으로 인코딩합니다.

대표 코드: `SliceResult`, `CursorResponse`, `CursorTokenResponse`, `SharedFeedCursor`

## SSE

Server-Sent Events입니다. 서버가 브라우저 연결을 유지하며 칭호, 댓글 알림, 크루 presence 이벤트를 푸시할 때 사용합니다.

대표 코드: `SseEmitterManager`, `SseAchievementNotifier`, `SseCommentNotificationNotifier`, `SseCrewPresenceNotifier`

## Rate Limit

요청 IP와 사용자 기준으로 API 호출량을 제한하는 기능입니다. Redis 기반 Bucket4j를 사용하며 세션 시작, 쓰기, 읽기 요청별 비용이 다릅니다.

대표 코드: `RateLimitInterceptor`, `RateLimiterService`

## Search Index

공유 세션을 검색하기 위한 색인 데이터입니다. 기본 검색은 MySQL fulltext이고, 설정에 따라 Elasticsearch 검색을 사용할 수 있습니다.

대표 코드: `SessionSearchPort`, `MySqlFulltextSearchAdapter`, `ElasticsearchSearchAdapter`, `ElasticsearchSessionIndexer`
