# Testing

이 문서는 현재 테스트 구조와 검증 방법을 설명합니다.

## 현재 테스트 파일 상태

현재 저장소에는 Java 테스트 클래스가 없고, 테스트 리소스만 존재합니다.

```text
deep-flow-api/src/test/resources/application-test.yml
```

즉 문서 작성 시점 기준으로 기능별 자동화 테스트는 아직 추가되지 않았습니다. 기능을 수정할 때는 관련 테스트를 새로 추가하거나, 최소한 빌드와 수동 검증 절차를 남겨야 합니다.

## 테스트 설정

`application-test.yml` 주요 값:

| 항목 | 값 |
| --- | --- |
| JPA ddl-auto | `create-drop` |
| Hibernate dialect | MySQL |
| JWT secret | 테스트 기본값 |
| CORS origin | `http://localhost:3000` |
| rate limit | 사실상 무제한 |
| R2 endpoint | `http://localhost:9000` |

## 검증 명령

전체 테스트:

```bash
./gradlew test
```

전체 빌드:

```bash
./gradlew build
```

모듈별 테스트:

```bash
./gradlew :deep-flow-api:test
./gradlew :deep-flow-application:test
./gradlew :deep-flow-domain:test
./gradlew :deep-flow-infra:test
```

애플리케이션 실행:

```bash
docker compose up -d
./gradlew bootRun
```

## 기능 수정 시 권장 테스트 위치

| 수정 영역 | 권장 테스트 |
| --- | --- |
| Controller 요청/응답, 인증 | `deep-flow-api/src/test` |
| Application 유스케이스 | `deep-flow-application/src/test` |
| Domain 상태 변경 규칙 | `deep-flow-domain/src/test` |
| JPA query, Redis, 검색, R2 | `deep-flow-infra/src/test` 또는 통합 테스트 |

## 우선 추가하면 좋은 테스트

현재 자동화 테스트가 거의 없으므로, 다음 흐름부터 우선 테스트하는 것이 좋습니다.

1. 세션 시작 중복 방지
2. 세션 종료 후 통계 갱신
3. 크루 가입 최대 인원 동시성
4. 세션 공유 가능 조건과 중복 공유 방지
5. 댓글 작성 시 멘션 저장과 알림 대상 계산
6. 반응 토글
7. 검색 타입별 검증
8. Outbox 실패 재시도
9. 대표 칭호 설정 권한
10. API 에러 응답 포맷

## 수동 검증 체크리스트

자동화 테스트가 없는 흐름을 수정할 때는 최소한 다음을 기록합니다.

- 실행한 API
- 사용한 사용자와 크루 상태
- 기대 응답 status와 body
- DB에 저장된 주요 row
- 캐시나 Outbox가 관여하면 후처리 결과
- SSE가 관여하면 연결 이벤트와 수신 이벤트 이름

## 문서와 테스트의 관계

문서의 `03-use-cases/`와 `02-architecture/03-data-flow.md`에 적힌 흐름은 테스트 케이스 후보입니다. 기능을 수정할 때는 해당 유스케이스의 “주의점”을 테스트 이름으로 바꾸면 테스트 누락을 줄일 수 있습니다.
