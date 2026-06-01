# Project Overview

## 목적

Deep Flow Backend는 사용자의 집중 세션 기록을 중심으로 크루 공유, 댓글과 반응, 통계, 칭호, 알림, 검색을 제공하는 백엔드입니다.

핵심 흐름은 사용자가 집중 세션을 시작하고 종료한 뒤, 세션 로그를 작성하고 필요한 경우 크루에 공유하는 것입니다. 공유된 세션은 크루 피드, 댓글, 반응, 태그, 검색, 하이라이트, 알림 흐름과 연결됩니다.

## 주요 기능 영역

| 영역 | 설명 | 대표 코드 |
| --- | --- | --- |
| 인증 | 회원가입, 로그인, 토큰 재발급, 로그아웃 | `AuthController`, `AuthService`, `JwtProvider` |
| 집중 세션 | 세션 시작, 종료, 로그 수정, 삭제, 상세 조회 | `SessionController`, `SessionService`, `FocusSession` |
| 세션 공유 | 종료된 세션을 크루에 공유하고 태그를 관리 | `SessionShareController`, `SessionShareService`, `SessionTag` |
| 크루 | 크루 생성, 수정, 가입, 초대 코드, 멤버 관리, 활동 조회 | `CrewController`, `CrewService`, `Crew`, `CrewMember` |
| 크루 피드 | 공유 세션 목록, 공유 세션 상세, 하이라이트 조회 | `CrewFeedController`, `CrewFeedService`, `CrewHighlightService` |
| 댓글과 반응 | 공유 세션 댓글, 대댓글, 멘션, 이모지 반응 | `SessionCommentService`, `SessionReactionService` |
| 알림 | 댓글과 멘션 알림, 칭호 달성 SSE | `NotificationController`, `CommentNotificationListener`, `AchievementNotifier` |
| 통계 | 일별 집중 통계, 대시보드, 캘린더, 시간대 분포 | `StatsController`, `DailyFocusStatsService`, `StatsDashboardService` |
| 칭호 | 세션 종료와 로그 수정 후 칭호 조건 평가 | `AchievementService`, `AchievementEvaluator` 구현체 |
| 검색 | 공유 세션 검색과 검색 색인 반영 | `SearchService`, `SessionSearchPort`, `SessionIndexer` |
| 이미지 | 로그 이미지 업로드와 삭제 | `ImageController`, `ImageService`, `R2ImageStorage` |

## 모듈 구성

```text
deep-flow-api
  HTTP API, 인증 필터, Controller, Request/Response DTO, 예외 응답

deep-flow-application
  유스케이스 서비스, 트랜잭션 경계, 포트 인터페이스, 이벤트 처리

deep-flow-domain
  JPA 도메인 엔티티, 상태 변경 규칙, 도메인 이벤트, enum

deep-flow-infra
  JPA 저장소 구현, Redis, Redisson 락, Elasticsearch/MySQL 검색, JWT, R2, SSE
```

## 전체 요청 흐름

```text
Client
→ deep-flow-api Controller
→ deep-flow-application Service
→ deep-flow-domain Entity
→ deep-flow-application Port
→ deep-flow-infra Adapter
→ DB / Redis / Elasticsearch / R2 / SSE
```

API 계층은 HTTP 요청과 응답을 담당하고, Application 계층은 하나의 유스케이스 흐름과 트랜잭션을 조합합니다. Domain 계층은 핵심 상태와 규칙을 표현하며, Infra 계층은 데이터베이스와 외부 기술 구현을 담당합니다.

## 프로젝트를 읽는 방법

처음에는 `Controller`에서 시작해 연결된 Application 서비스를 찾고, 서비스가 호출하는 도메인 메서드와 포트 인터페이스를 따라가면 됩니다.

예를 들어 세션 시작 흐름은 다음 순서로 읽습니다.

```text
SessionController#start
→ SessionService#startSession
→ FocusSession#create
→ SessionRepository#save
→ SessionStartedEvent
```

복잡한 기능은 이벤트, 캐시, 락, Outbox가 함께 등장합니다. 이런 흐름은 [../02-architecture/03-data-flow.md](../02-architecture/03-data-flow.md)에서 별도로 설명합니다.
