# Deep Flow Backend Docs

이 문서는 Deep Flow 백엔드를 처음 보는 개발자가 프로젝트의 목적, 도메인, 계층 구조, 주요 기능 흐름, 실행 방법을 순서대로 이해하도록 만든 문서입니다.

## 처음 읽는 순서

1. [01-getting-started/01-overview.md](01-getting-started/01-overview.md)
2. [01-getting-started/02-local-dev.md](01-getting-started/02-local-dev.md)
3. [01-getting-started/03-glossary.md](01-getting-started/03-glossary.md)
4. [02-architecture/01-module-structure.md](02-architecture/01-module-structure.md)
5. [02-architecture/02-domain-model.md](02-architecture/02-domain-model.md)
6. [03-use-cases/README.md](03-use-cases/README.md)
7. [03-use-cases/01-auth.md](03-use-cases/01-auth.md)
8. [03-use-cases/02-session.md](03-use-cases/02-session.md)
9. [03-use-cases/03-crew.md](03-use-cases/03-crew.md)
10. [03-use-cases/04-collaboration.md](03-use-cases/04-collaboration.md)
11. [03-use-cases/05-stats-achievement.md](03-use-cases/05-stats-achievement.md)
12. [03-use-cases/06-media.md](03-use-cases/06-media.md)
13. [02-architecture/03-data-flow.md](02-architecture/03-data-flow.md)
14. [02-architecture/04-runtime-behavior.md](02-architecture/04-runtime-behavior.md)
15. [02-architecture/05-infrastructure.md](02-architecture/05-infrastructure.md)
16. [04-reference/02-error-handling.md](04-reference/02-error-handling.md)
17. [04-reference/01-api-map.md](04-reference/01-api-map.md)
18. [04-reference/03-testing.md](04-reference/03-testing.md)
19. [05-decisions/](05-decisions/)

## 문서 분류

| 분류 | 역할 |
| --- | --- |
| [01-getting-started](01-getting-started/) | 프로젝트 진입용 문서 |
| [02-architecture](02-architecture/) | 모듈, 도메인, 데이터 흐름, 런타임 구조 |
| [03-use-cases/README.md](03-use-cases/README.md) | 기능별 요청 흐름 추적 |
| [04-reference](04-reference/) | API, 예외, 테스트 같은 참고 문서 |
| [05-decisions](05-decisions/) | 주요 설계 결정 기록 |

## 문서 목적

이 문서는 패키지 목록을 나열하는 소개 문서가 아니라, Deep Flow 백엔드가 어떤 도메인 규칙을 가지고 있고 요청이 어떤 코드 흐름을 타는지 이해하기 위한 지도입니다.

핵심 읽기 관점은 다음과 같습니다.

- 이 서비스가 해결하는 문제가 무엇인지
- 주요 도메인 모델이 어떻게 연결되는지
- 요청이 API, Application, Domain, Infra 계층을 어떻게 통과하는지
- 트랜잭션, 락, 캐시, 이벤트, 검색 색인, 알림 같은 경계가 어디에 있는지
- 왜 멀티모듈과 포트 구조를 사용하는지

## 문서별 역할

| 문서 | 답하는 질문 |
| --- | --- |
| [01-getting-started/01-overview.md](01-getting-started/01-overview.md) | 이 프로젝트가 무엇을 하는가 |
| [01-getting-started/02-local-dev.md](01-getting-started/02-local-dev.md) | 로컬에서 어떻게 실행하고 검증하는가 |
| [01-getting-started/03-glossary.md](01-getting-started/03-glossary.md) | 코드에서 반복되는 용어가 무슨 뜻인가 |
| [02-architecture/01-module-structure.md](02-architecture/01-module-structure.md) | 왜 이런 모듈과 계층으로 나누었는가 |
| [02-architecture/02-domain-model.md](02-architecture/02-domain-model.md) | 핵심 도메인 모델과 규칙은 무엇인가 |
| [03-use-cases/README.md](03-use-cases/README.md) | 주요 기능 요청은 어떤 코드 흐름으로 처리되는가 |
| [02-architecture/03-data-flow.md](02-architecture/03-data-flow.md) | 복잡한 비동기, 캐시, 검색, 알림 흐름은 어떻게 동작하는가 |
| [02-architecture/04-runtime-behavior.md](02-architecture/04-runtime-behavior.md) | 캐시, 비동기, 스케줄러, SSE, 락은 런타임에 어떻게 동작하는가 |
| [02-architecture/05-infrastructure.md](02-architecture/05-infrastructure.md) | DB, Redis, 검색, R2, JWT, SSE 구현은 어디에 있는가 |
| [04-reference/02-error-handling.md](04-reference/02-error-handling.md) | 예외와 API 에러 응답은 어떻게 매핑되는가 |
| [04-reference/01-api-map.md](04-reference/01-api-map.md) | API 진입점은 어느 Controller와 Application 서비스로 연결되는가 |
| [04-reference/03-testing.md](04-reference/03-testing.md) | 현재 테스트 구조와 검증 명령은 무엇인가 |
| [05-decisions/](05-decisions/) | 중요한 설계 결정을 왜 했는가 |

## 문서 관리 규칙

문서는 코드와 함께 관리합니다. 모든 문서를 매번 갱신하지 않고, 변경 영향이 있는 문서만 함께 수정합니다.

### 문서 수정이 필요한 경우

- API가 추가되거나 변경된 경우
- Controller 진입점이 바뀐 경우
- 유스케이스 흐름이나 트랜잭션 범위가 바뀐 경우
- 도메인 규칙이 바뀐 경우
- DB, Redis, 검색, 알림, SSE, 캐시, 락 흐름이 바뀐 경우
- 로컬 실행 방법, 환경변수, 인프라 구성이 바뀐 경우
- 중요한 설계 결정이 추가된 경우

### 문서 수정 기준

- 단순 오타 수정이나 내부 변수명 변경은 문서 수정 대상이 아님
- 요청 흐름, 책임 위치, 외부 연동 방식이 바뀌면 관련 문서를 수정
- 기존 결정의 설명을 다시 쓰기보다 새로운 결정은 `05-decisions`에 추가
- 코드 변경 시 영향 문서를 PR 체크리스트로 한 번씩 확인

### 문서 갱신 우선순위

| 변경 내용 | 우선 확인 문서 |
| --- | --- |
| 새 API 추가 | `04-reference/01-api-map.md` |
| Controller 변경 | `04-reference/01-api-map.md` |
| Service 유스케이스 흐름 변경 | `03-use-cases/README.md`와 해당 도메인 문서 |
| 도메인 규칙 변경 | `02-architecture/02-domain-model.md`와 관련 use-case |
| 모듈 의존성 변경 | `02-architecture/01-module-structure.md` |
| Redis, DB, 검색, 알림 구조 변경 | `02-architecture/03-data-flow.md`, `02-architecture/05-infrastructure.md`, `02-architecture/04-runtime-behavior.md` |
| 로컬 실행 방법 변경 | `01-getting-started/02-local-dev.md` |
| 중요한 설계 결정 | `05-decisions/` |
