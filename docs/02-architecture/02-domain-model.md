# Domain Model

## 전체 관계 요약

```text
User
├── FocusSession
│   ├── FocusLog
│   │   └── FocusLogImage
│   ├── SessionTag
│   ├── SessionComment
│   │   └── CommentMention
│   └── SessionReaction
├── CrewMember
│   └── Crew
├── DailyFocusStats
└── UserAchievement
    └── Achievement

OutboxEvent
└── 공유 세션 검색 색인 후처리
```

## User

`User`는 인증과 사용자 프로필의 기준 엔티티입니다.

주요 속성:
- `username`
- `password`
- `name`
- `role`
- `refreshToken`
- `displayAchievement`

주요 규칙:
- 로그인 성공 시 `afterLogin`으로 refresh token을 갱신할 수 있음
- 대표 칭호는 사용자가 달성한 칭호만 설정해야 하며 이 검증은 `AchievementService#updateDisplayAchievement`에서 수행

## FocusSession

`FocusSession`은 사용자의 집중 활동 한 건을 표현합니다.

주요 속성:
- `user`
- `startTime`
- `endTime`
- `durationSeconds`
- `status`
- `focusLog`
- `sharedCrewId`
- `sharedAt`
- `deletedAt`

주요 규칙:
- `create` 시 상태는 진행 중 세션으로 시작
- `stop` 시 종료 시간과 집중 시간이 확정됨
- 진행 중 세션은 삭제할 수 없음
- 종료된 세션만 공유 가능
- 공유 상태는 `sharedCrewId`와 `sharedAt`으로 표현

관련 유스케이스:
- `SessionService#startSession`
- `SessionService#stopSession`
- `SessionService#updateLog`
- `SessionShareService#shareLockedInternal`

## FocusLog

`FocusLog`는 세션의 기록 내용입니다.

주요 속성:
- `title`
- `content`
- `summary`
- `images`

주요 규칙:
- 세션과 함께 생성됨
- 로그 수정 시 기존 이미지와 새 이미지 목록을 비교해 제거된 이미지를 스토리지에서 삭제
- 로그 수정 이벤트는 칭호 평가 트리거가 됨

## Crew

`Crew`는 집중 세션을 공유하는 그룹입니다.

주요 속성:
- `name`
- `description`
- `ownerUserId`
- `visibility`
- `maxMembers`
- `inviteCode`
- `inviteCodeExpiresAt`
- `deletedAt`

주요 규칙:
- 생성자는 `CrewMember` 소유자로 함께 등록됨
- 공개 크루만 직접 가입 가능
- 초대 코드는 만료 시간이 지나면 유효하지 않음
- 크루 해체는 soft delete와 멤버십 삭제를 함께 수행
- 최대 인원은 현재 멤버 수보다 작게 줄일 수 없음

관련 유스케이스:
- `CrewService#create`
- `CrewService#issueInviteCode`
- `CrewService#joinByCode`
- `CrewService#joinPublic`
- `CrewService#disband`

## CrewMember

`CrewMember`는 크루와 사용자의 관계입니다.

주요 속성:
- `crewId`
- `userId`
- `role`

주요 규칙:
- 소유자는 일반 탈퇴할 수 없음
- 소유자만 크루 수정, 해체, 멤버 추방 가능
- 가입은 크루별 분산 락 안에서 처리해 최대 인원 초과를 방지

## SessionTag

`SessionTag`는 공유 세션에 붙는 태그입니다.

주요 규칙:
- 세션당 최대 태그 수는 `SessionShareService.MAX_TAGS`
- 태그는 `TagNormalizer`를 거쳐 공백 제거, 정규화, 중복 제거 후 저장
- 공유 철회 시 기존 태그는 함께 삭제

## SessionComment

`SessionComment`는 공유 세션 댓글입니다.

주요 속성:
- `sessionId`
- `parentId`
- `author`
- `content`
- `deletedAt`

주요 규칙:
- 댓글 작성자는 공유 세션이 속한 크루의 멤버여야 함
- 대댓글의 부모 댓글은 같은 세션에 속해야 함
- 삭제는 soft delete로 처리하며, 삭제된 부모 댓글도 대댓글이 있으면 트리 구조에 남을 수 있음

## CommentMention

`CommentMention`은 댓글에 언급된 사용자를 저장합니다.

주요 규칙:
- 댓글 작성자 본인 멘션은 제거
- 중복 멘션은 제거
- 알림 발송 시 실제 크루 멤버만 대상으로 처리
- 읽음 처리는 `NotificationService`가 담당

## SessionReaction

`SessionReaction`은 공유 세션의 이모지 반응입니다.

주요 규칙:
- 세션, 사용자, 이모지 조합을 기준으로 토글
- 반응 추가와 제거는 크루 하이라이트 캐시 무효화 이벤트가 됨

## DailyFocusStats

`DailyFocusStats`는 사용자별 일별 집중 통계입니다.

주요 속성:
- `userId`
- `date`
- `totalSessions`
- `totalDurationSeconds`

주요 규칙:
- 세션 종료 후 커밋 이후 비동기로 갱신
- 자정을 넘긴 세션은 날짜별로 시간을 나눠 반영
- 첫 날짜에만 세션 수를 1 증가시키고 이후 날짜는 시간만 더함

## Achievement

`Achievement`는 칭호 정의이고 `UserAchievement`는 사용자의 달성 기록입니다.

주요 규칙:
- 세션 종료와 로그 수정 트리거별로 평가 가능한 Evaluator만 실행
- 동시 평가 중복 지급은 유니크 제약과 `DataIntegrityViolationException` 처리로 방어
- 새로 지급된 칭호는 SSE 알림 대상

## OutboxEvent

`OutboxEvent`는 외부 후처리를 위한 이벤트 저장소입니다.

주요 속성:
- `aggregateType`
- `aggregateId`
- `eventType`
- `payload`
- `status`
- `retryCount`
- `lastError`

주요 규칙:
- 세션 공유, 공유 철회, 태그 변경 시 검색 색인 이벤트를 저장
- 워커가 pending 이벤트를 읽어 색인 반영 후 성공 또는 실패 상태로 변경
- 실패 이벤트는 최대 재시도 횟수까지 다시 처리
