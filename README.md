# Deep Flow Backend

Deep Flow Backend는 사용자의 집중 활동을 기록하고 공유할 수 있는 서비스입니다.

집중 세션 관리, 크루 기반 협업, 실시간 알림, 활동 분석, 칭호 시스템 등의 기능을 제공합니다.

도메인 규칙과 유스케이스를 중심으로 설계했으며, 외부 기술 의존성은 Port-Adapter 패턴으로 분리했습니다. Gradle 멀티 모듈 구조를 기반으로 API, Application, Domain, Infra 계층의 책임을 분리했습니다.

---

## 주요 기능

| 영역    | 기능                                 |
| ----- | ---------------------------------- |
| 인증    | 회원가입, 로그인, JWT 기반 인증 및 재발급, 로그아웃   |
| 집중 세션 | 세션 시작, 종료, 목록 조회, 상세 조회, 삭제        |
| 집중 로그 | 제목, 내용, 요약, 이미지 수정                 |
| 크루    | 생성, 수정, 공개 가입, 초대 코드 가입, 멤버 관리     |
| 공유    | 종료된 세션 공유, 태그 수정, 공유 철회, 크루 피드     |
| 상호작용  | 댓글, 대댓글, 멘션, 이모지 반응                |
| 알림    | 댓글, 멘션, 칭호 달성, SSE 기반 실시간 알림       |
| 통계    | 오늘/주간 요약, 최근 7일 통계, 시간대·요일 분석      |
| 칭호    | 활동 기반 칭호 평가                        |
| 검색    | MySQL Fulltext 또는 Elasticsearch 검색 |
| 이미지   | 집중 로그 이미지 업로드 및 정리                 |

---

## 아키텍처

### 모듈 구조

```text
deep-flow-be
├── .github
│   └── ISSUE_TEMPLATE
├── deep-flow-api
├── deep-flow-application
├── deep-flow-domain
├── deep-flow-infra
├── docs
├── gradle
├── infra
│   └── docker
├── monitoring
├── build.gradle.kts
├── docker-compose.yml
└── settings.gradle.kts
```

### 모듈 책임

| 모듈                    | 책임                                                               |
| --------------------- | ---------------------------------------------------------------- |
| deep-flow-api         | Spring Boot 실행 진입점, Controller, DTO, Security, Swagger, 전역 예외 처리 |
| deep-flow-application | 유스케이스 서비스, 트랜잭션 경계, 분산 락 진입점, 이벤트 처리, Port 정의                    |
| deep-flow-domain      | 도메인 엔티티, 상태 변경 규칙, Enum, 도메인 이벤트                                 |
| deep-flow-infra       | JPA, Redis, Redisson, Elasticsearch, JWT, R2, SSE 구현             |

### 의존성 구조

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
└── 다른 프로젝트 모듈 의존 없음
```

### 요청 흐름

```text
API
 ↓
Application
 ↓
Domain

Application
 ↓ Port
Infra
```

Application 계층은 외부 기술을 직접 참조하지 않고 Port 인터페이스를 통해 의존합니다. Infra 계층은 해당 Port를 구현하여 기술 의존성을 격리합니다.

---

## 기술 스택

| 분류            | 기술                                               |
| ------------- | ------------------------------------------------ |
| Language      | Java 21                                          |
| Framework     | Spring Boot 3.3.5                                |
| Build         | Gradle Kotlin DSL                                |
| Database      | MySQL 8                                          |
| ORM           | Spring Data JPA, Hibernate                       |
| Cache / Lock  | Redis, Redisson                                  |
| Search        | MySQL Fulltext, Elasticsearch                    |
| Auth          | Spring Security, JWT                             |
| Storage       | Cloudflare R2, AWS S3 SDK                        |
| Realtime      | Server-Sent Events                               |
| Observability | Spring Actuator, Micrometer, Prometheus, Grafana |
| API Docs      | springdoc-openapi, Swagger UI                    |
| Test          | JUnit 5, Testcontainers, Awaitility              |

---

## 로컬 실행

### 요구 사항

* JDK 21
* Docker
* Docker Compose

### 인프라 실행

```bash
docker compose up -d
```

실행되는 서비스

| 서비스           | 포트   | 용도                   |
| ------------- | ---- | -------------------- |
| MySQL 8       | 3307 | 애플리케이션 DB            |
| Redis         | 6379 | 캐시, Rate Limit, 분산 락 |
| Elasticsearch | 9200 | 공유 세션 검색             |
| Prometheus    | 9090 | 메트릭 수집               |
| Grafana       | 3001 | 모니터링 대시보드            |

### 애플리케이션 실행

```bash
./gradlew bootRun
```

Windows PowerShell

```powershell
.\gradlew.bat bootRun
```

기본 활성 프로필은 `local` 입니다.

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

---

## 로컬 기본 설정

| 항목              | 값                                     |
| --------------- | ------------------------------------- |
| DB URL          | jdbc:mysql://localhost:3307/demo      |
| DB Username     | root                                  |
| DB Password     | root                                  |
| Swagger UI      | http://localhost:8080/swagger-ui.html |
| OpenAPI Docs    | http://localhost:8080/api-docs        |
| Actuator Health | http://localhost:8080/actuator/health |
| Grafana         | http://localhost:3001                 |

---

## 환경 변수

| 이름                     | 설명                     | 기본값                   |
| ---------------------- | ---------------------- | --------------------- |
| SPRING_PROFILES_ACTIVE | 실행 프로필                 | local                 |
| JWT_SECRET             | JWT 서명 키               | local 프로필 기본값         |
| REDIS_HOST             | Redis Host             | localhost             |
| REDIS_PORT             | Redis Port             | 6379                  |
| ES_URIS                | Elasticsearch URI      | http://localhost:9200 |
| SEARCH_ENGINE          | mysql 또는 es            | mysql                 |
| OUTBOX_WORKER_ENABLED  | Outbox Worker 활성화      | true                  |
| R2_ENDPOINT            | Cloudflare R2 Endpoint | local 프로필 기본값         |
| R2_ACCESS_KEY          | R2 Access Key          | 없음                    |
| R2_SECRET_KEY          | R2 Secret Key          | 없음                    |
| R2_BUCKET              | Bucket 이름              | deep-flow             |
| R2_PUBLIC_URL          | Public URL             | local 프로필 기본값         |

실제 Cloudflare R2를 사용하려면 `R2_ACCESS_KEY`, `R2_SECRET_KEY`를 별도로 설정해야 합니다.

---

## 빌드 및 테스트

전체 테스트

```bash
./gradlew test
```

전체 빌드

```bash
./gradlew build
```

모듈별 테스트

```bash
./gradlew :deep-flow-api:test
./gradlew :deep-flow-application:test
./gradlew :deep-flow-domain:test
./gradlew :deep-flow-infra:test
```

---

## 문서

상세 설계 문서는 `docs` 디렉터리에서 관리합니다.

| 경로                      | 내용                    |
| ----------------------- | --------------------- |
| docs/01-getting-started | 프로젝트 개요, 로컬 개발, 용어    |
| docs/02-architecture    | 모듈 구조, 도메인 모델, 데이터 흐름 |
| docs/03-use-cases       | 인증, 세션, 크루, 협업, 통계    |
| docs/04-reference       | API 맵, 예외 처리, 테스트     |
| docs/05-decisions       | 아키텍처 의사결정 기록          |
