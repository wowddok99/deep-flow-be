# Collaboration Use Cases

## 댓글 작성과 멘션 알림

### 목적

공유 세션에 댓글이나 대댓글을 작성하고, 글 작성자와 멘션 대상에게 알림을 보냅니다.

### 흐름 요약

```text
POST /api/v1/sessions/{sessionId}/comments
→ SessionCommentController#create
→ SessionCommentService#create
→ 공유 세션과 크루 멤버십 확인
→ 부모 댓글 세션 일치 확인
→ SessionComment#create
→ CommentMention 저장
→ SessionCommentCreatedEvent 발행
→ AFTER_COMMIT CommentNotificationListener
→ CommentNotificationNotifier
```

### 주의점

- 댓글은 공유된 세션에만 작성 가능
- 작성자는 공유 크루의 멤버여야 함
- 대댓글의 부모 댓글은 같은 세션에 속해야 함
- 작성자 본인 멘션은 제거
- 글 작성자가 이미 댓글 알림을 받았다면 같은 댓글의 멘션 알림은 중복 발송하지 않음

### 관련 코드

- API: `SessionCommentController`, `CommentController`
- Application: `SessionCommentService`, `CommentNotificationListener`
- Domain: `SessionComment`, `CommentMention`, `SessionCommentCreatedEvent`
- Persistence: `SessionCommentRepository`, `CommentMentionRepository`, `CrewMemberRepository`, `UserRepository`
- Notification: `CommentNotificationNotifier`

### 갱신 기준

- 댓글 작성, 수정, 삭제 규칙이 바뀌는 경우
- 멘션 대상 선정이나 중복 발송 규칙이 바뀌는 경우
- 댓글 알림 전송 타이밍이나 채널이 바뀌는 경우
- 대댓글 구조나 부모 댓글 검증이 바뀌는 경우

## 반응 토글

### 목적

공유 세션에 이모지 반응을 추가하거나 제거합니다.

### 흐름 요약

```text
POST /api/v1/sessions/{sessionId}/reactions
→ SessionReactionController#toggle
→ SessionReactionService#toggle
→ 공유 세션과 멤버십 확인
→ ReactionEmoji#fromUnicode
→ 기존 반응 있으면 삭제, 없으면 저장
→ SessionReactionAddedEvent 또는 SessionReactionRemovedEvent
```

### 주의점

- 허용된 이모지만 반응으로 사용할 수 있음
- 반응 변경은 크루 하이라이트 캐시 무효화 대상

### 관련 코드

- API: `SessionReactionController`
- Application: `SessionReactionService`
- Domain: `SessionReaction`, `ReactionEmoji`, `SessionReactionAddedEvent`, `SessionReactionRemovedEvent`
- Persistence: `SessionReactionRepository`, `CrewMemberRepository`, `UserRepository`

### 문서 반영 기준

- 반응 가능한 emoji 목록이 바뀌는 경우
- 토글 규칙이나 집계 방식이 바뀌는 경우
- 하이라이트 캐시 무효화 이벤트가 바뀌는 경우
