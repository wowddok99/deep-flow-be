# Local Development

## 요구사항

- Java 21
- Gradle Wrapper
- Docker
- Docker Compose

## 로컬 인프라 실행

```bash
docker compose up -d
```

로컬 Compose는 다음 서비스를 실행합니다.

| 서비스 | 포트 | 용도 |
| --- | --- | --- |
| MySQL 8 | `3307` | 애플리케이션 DB |
| Redis | `6379` | 캐시, rate limit, 분산 락 |
| Elasticsearch | `9200` | 공유 세션 검색 |
| Prometheus | `9090` | 메트릭 수집 |
| Grafana | `3001` | 대시보드 |

## 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 프로필은 `local`입니다.

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

## 주요 로컬 설정

`deep-flow-api/src/main/resources/application-local.yml`

| 항목 | 값 |
| --- | --- |
| DB URL | `jdbc:mysql://localhost:3307/demo` |
| DB username | `root` |
| DB password | `root` |
| JPA ddl-auto | `update` |
| SQL init | `always` |
| CORS origin | `http://localhost:3000` |
| local JWT secret | 설정 파일에 기본값 존재 |

## 주요 환경변수

| 이름 | 설명 | 비고 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | 실행 프로필 | 기본값 `local` |
| `JWT_SECRET` | JWT 서명 키 | prod에서는 필수 |
| `REDIS_HOST` | Redis host | 기본값 `localhost` |
| `REDIS_PORT` | Redis port | 기본값 `6379` |
| `ES_URIS` | Elasticsearch URI | 기본값 `http://localhost:9200` |
| `SEARCH_ENGINE` | 검색 엔진 선택 | 기본값 `mysql` |
| `OUTBOX_WORKER_ENABLED` | Outbox worker 활성화 | 기본값 `true` |
| `R2_ENDPOINT` | Cloudflare R2 endpoint | 이미지 업로드에 필요 |
| `R2_ACCESS_KEY` | R2 access key | 이미지 업로드에 필요 |
| `R2_SECRET_KEY` | R2 secret key | 이미지 업로드에 필요 |
| `R2_BUCKET` | R2 bucket | 기본값 `deep-flow` |
| `R2_PUBLIC_URL` | R2 public URL | 이미지 응답 URL 구성 |

## 테스트 실행

전체 테스트:

```bash
./gradlew test
```

모듈별 테스트:

```bash
./gradlew :deep-flow-api:test
./gradlew :deep-flow-application:test
./gradlew :deep-flow-domain:test
./gradlew :deep-flow-infra:test
```

## 빌드

```bash
./gradlew build
```

## Swagger

애플리케이션 실행 후 확인합니다.

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/api-docs
```

## Actuator와 모니터링

노출 endpoint:

```text
/actuator/health
/actuator/prometheus
/actuator/metrics
/actuator/info
```

Prometheus는 `monitoring/prometheus.yml`을 사용하고, Grafana는 `monitoring/grafana` 아래 provisioning 설정과 dashboard를 사용합니다.

## 자주 확인할 문제

### MySQL 연결 실패

```bash
docker compose ps
```

MySQL 컨테이너가 떠 있고 `3307:3306` 포트가 열려 있는지 확인합니다.

### Redis 연결 실패

Redis 컨테이너와 `REDIS_HOST`, `REDIS_PORT` 설정을 확인합니다.

### Elasticsearch 검색이 안 되는 경우

`SEARCH_ENGINE` 값과 Elasticsearch 컨테이너 상태를 확인합니다. 기본값은 `mysql`이므로 Elasticsearch를 사용하려면 설정을 명시해야 합니다.

### 이미지 업로드 실패

R2 관련 환경변수가 없으면 이미지 업로드가 실패할 수 있습니다. 세션 로그 이미지 기능을 테스트할 때는 R2 환경변수를 설정합니다.
