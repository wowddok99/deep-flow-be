# API Map

이 문서는 상세 API 명세가 아니라 Controller와 Application 유스케이스 연결을 빠르게 찾기 위한 지도입니다. 상세 요청/응답 스키마는 Swagger UI와 DTO 코드를 함께 확인합니다.

Swagger:
- `/swagger-ui.html`
- `/api-docs`

## Auth

### `AuthController`

Base path: `/api/v1/auth`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| POST | `/signup` | 회원가입 | `AuthService#signup` |
| POST | `/login` | 로그인 | `AuthService#login` |
| POST | `/reissue` | 토큰 재발급 | `AuthService#reissue` |
| POST | `/logout` | 로그아웃 | `AuthService#logout` |

## Session

### `SessionController`

Base path: `/api/v1/sessions`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| POST | `/start` | 집중 세션 시작 | `SessionService#startSession` |
| GET | `` | 내 세션 목록 | `SessionService#getAllSessions` |
| GET | `/{id}` | 내 세션 상세 | `SessionService#getSessionDetail` |
| PUT | `/{id}/log` | 집중 로그 수정 | `SessionService#updateLog` |
| POST | `/{id}/stop` | 집중 세션 종료 | `SessionService#stopSession` |
| DELETE | `/{id}` | 세션 삭제 | `SessionService#deleteSession` |

### `SessionShareController`

Base path: `/api/v1/sessions/{sessionId}/share`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| POST | `` | 세션 공유 | `SessionShareLocker#share` |
| PUT | `/tags` | 공유 태그 수정 | `SessionShareLocker#updateTags` |
| DELETE | `` | 공유 철회 | `SessionShareLocker#unshare` |

### `SessionCommentController`

Base path: `/api/v1/sessions/{sessionId}/comments`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `` | 댓글 목록 | `SessionCommentService#getComments` |
| POST | `` | 댓글 작성 | `SessionCommentService#create` |

### `CommentController`

Base path: `/api/v1/comments/{commentId}`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| PATCH | `` | 댓글 수정 | `SessionCommentService#update` |
| DELETE | `` | 댓글 삭제 | `SessionCommentService#delete` |

### `SessionReactionController`

Base path: `/api/v1/sessions/{sessionId}/reactions`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| POST | `` | 반응 토글 | `SessionReactionService#toggle` |
| GET | `` | 반응 집계 | `SessionReactionService#aggregate` |

### `UserTagController`

Base path: `/api/v1/users/me/tags`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `/recent` | 내 최근 태그 | `SessionTagService#getMyRecentTags` |

## Crew

### `CrewController`

Base path: `/api/v1/crews`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| POST | `` | 크루 생성 | `CrewService#create` |
| GET | `` | 내 크루 목록 | `CrewService#listMyCrews` |
| GET | `/{crewId}` | 크루 상세 | `CrewService#getDetail` |
| GET | `/search` | 공개 크루 검색 | `CrewService#searchPublic` |
| PATCH | `/{crewId}` | 크루 수정 | `CrewService#update` |
| DELETE | `/{crewId}` | 크루 해체 | `CrewService#disband` |
| POST | `/{crewId}/invite` | 초대 코드 발급 | `CrewService#issueInviteCode` |
| POST | `/join` | 초대 코드 가입 | `CrewService#joinByCode` |
| POST | `/{crewId}/join` | 공개 크루 가입 | `CrewService#joinPublic` |
| DELETE | `/{crewId}/members/me` | 크루 탈퇴 | `CrewService#leave` |
| DELETE | `/{crewId}/members/{userId}` | 멤버 추방 | `CrewService#kick` |
| GET | `/{crewId}/activity` | 크루 활동 조회 | `CrewService#getActivity` |

### `CrewFeedController`

Base path: `/api/v1/crews/{crewId}`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `/feed` | 크루 공유 세션 피드 | `CrewFeedService#getFeed` |
| GET | `/sessions/{sessionId}` | 공유 세션 상세 | `CrewFeedService#getSharedSession` |

### `CrewHighlightController`

Base path: `/api/v1/crews/{crewId}/highlights`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `` | 크루 하이라이트 | `CrewHighlightService#getHighlight` |

### `CrewSearchController`

Base path: `/api/v1/crews/{crewId}/search`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `` | 공유 세션 검색 | `SearchService#search` |

### `CrewTagController`

Base path: `/api/v1/crews/{crewId}/tags`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `` | 인기 태그 | `SessionTagService#getPopularTags` |
| GET | `/suggest` | 태그 자동완성 | `SessionTagService#suggestTags` |

### `CrewMemberSuggestController`

Base path: `/api/v1/crews/{crewId}/members`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `/suggest` | 멘션 대상 멤버 추천 | `CrewMemberSuggestService#suggestMembers` |

### Presence Controllers

| Controller | Base path | Method | Path | 역할 |
| --- | --- | --- | --- | --- |
| `CrewPresenceController` | `/api/v1/crews/presence` | GET | `/stream` | 크루 presence SSE |
| `CrewLivePresenceController` | `/api/v1/crews/{crewId}/presence` | GET | `/live` | 현재 집중 중 멤버 조회 |

## Stats

### `StatsController`

Base path: `/api/v1/stats`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `/overview` | 오늘과 주간 요약 | `DailyFocusStatsService#getOverview` |
| GET | `/weekly` | 최근 7일 통계 | `DailyFocusStatsService#getWeeklyStats` |
| GET | `/dashboard` | 대시보드 요약 | `StatsDashboardService#getDashboardOverview` |
| GET | `/weekly-trend` | 주간 추이 | `StatsDashboardService#getWeeklyTrend` |
| GET | `/day-of-week` | 요일별 분포 | `StatsDashboardService#getDayOfWeekDistribution` |
| GET | `/hourly` | 시간대별 분포 | `StatsDashboardService#getHourlyDistribution` |
| GET | `/calendar` | 캘린더 데이터 | `StatsDashboardService#getCalendarData` |
| GET | `/activity` | 로그 활동 | `StatsDashboardService#getLogActivity` |
| GET | `/all` | 통계 대시보드 전체 | `StatsDashboardService` 조합 |

## Achievement

### `AchievementController`

Base path: `/api/v1/achievements`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `` | 전체 칭호 목록과 달성 여부 | `AchievementService#getAllAchievements` |
| GET | `/me` | 내 칭호 목록 | `AchievementService#getMyAchievements` |
| PUT | `/display` | 대표 칭호 변경 | `AchievementService#updateDisplayAchievement` |
| GET | `/stream` | 칭호 SSE | `AchievementStreamManager` |

## Notification

### `NotificationController`

Base path: `/api/v1/notifications`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| GET | `/comments/stream` | 댓글 알림 SSE | `CommentNotificationNotifier` |
| GET | `/unread` | 읽지 않은 댓글/멘션 알림 | `NotificationService#getUnread` |
| PATCH | `/{id}/read` | 알림 읽음 처리 | `NotificationService#markRead` |
| PATCH | `/read-all` | 전체 읽음 처리 | `NotificationService#markAllRead` |

## Image

### `ImageController`

Base path: `/api/v1/images`

| Method | Path | 역할 | Application |
| --- | --- | --- | --- |
| POST | `` | 이미지 업로드 | `ImageService#upload` |
