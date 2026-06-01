# ADR-001. 멀티모듈 구조를 사용한다

## 상태

Accepted

## 배경

Deep Flow Backend는 인증, 집중 세션, 크루, 통계, 칭호, 알림, 검색, 이미지 저장을 함께 다룹니다.

한 모듈에 Controller, 유스케이스, 도메인 규칙, JPA, Redis, 검색, 스토리지 코드가 모두 섞이면 기능 수정 시 영향 범위를 파악하기 어렵고, 도메인 규칙이 외부 기술 구현에 끌려가기 쉽습니다.

## 결정

프로젝트를 다음 네 모듈로 분리합니다.

- `deep-flow-api`
- `deep-flow-application`
- `deep-flow-domain`
- `deep-flow-infra`

의존성 방향은 API와 Infra가 Application을 사용하고, Application은 Domain과 Port에 의존하도록 둡니다.

## 결과

장점:
- HTTP 표현, 유스케이스, 도메인 규칙, 외부 기술 구현의 책임이 분리됨
- 도메인 모델이 Redis, Elasticsearch, R2 같은 기술에 직접 의존하지 않음
- Application 서비스에서 트랜잭션과 외부 포트 호출 흐름을 찾기 쉬움
- 저장소와 외부 연동 구현을 Infra로 격리할 수 있음

단점:
- 단순 기능도 여러 모듈을 오가며 읽어야 함
- 새 개발자가 초기 구조를 이해하는 비용이 있음
- DTO와 포트, 구현체 사이의 매핑 코드가 늘어날 수 있음
