# ADR-002. Application Port로 외부 기술 경계를 분리한다

## 상태

Accepted

## 배경

Application 계층은 세션 시작, 크루 가입, 세션 공유, 댓글 작성 같은 유스케이스를 조합합니다. 이 계층이 JPA, Redis, Elasticsearch, JWT, R2, SSE 구현에 직접 의존하면 기술 변경이 유스케이스 코드 전반으로 퍼집니다.

예를 들어 검색 구현은 Elasticsearch와 MySQL Fulltext 중 선택될 수 있고, 이미지 저장소는 R2가 아닌 다른 S3 호환 스토리지로 바뀔 수 있습니다.

## 결정

Application 계층에 `port.out` 인터페이스를 두고, Infra 계층이 이를 구현합니다.

대표 포트:
- `UserRepository`
- `SessionRepository`
- `CrewRepository`
- `StatsRepository`
- `SessionSearchPort`
- `SessionIndexer`
- `ImageStorage`
- `TokenProvider`
- `RateLimiter`
- `AchievementNotifier`
- `CommentNotificationNotifier`

## 결과

장점:
- 유스케이스 코드는 기술 구현보다 도메인 흐름에 집중할 수 있음
- 검색, 스토리지, 알림, 토큰 구현 교체의 영향이 줄어듦
- 테스트에서 포트 단위로 대체 구현을 넣기 쉬움

단점:
- 인터페이스와 구현체를 함께 찾아야 하므로 파일 수가 늘어남
- 단순 저장소 호출도 Port와 Adapter를 거쳐야 함
- Port 메서드가 유스케이스 요구보다 넓어지면 Application과 Infra의 결합도가 다시 커질 수 있음
