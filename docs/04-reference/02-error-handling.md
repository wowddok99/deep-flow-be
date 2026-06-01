# Error Handling

이 문서는 예외가 API 응답으로 변환되는 방식을 설명합니다.

## 공통 응답 형식

성공 응답:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

실패 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "SESSION_NOT_FOUND",
    "message": "Session not found"
  }
}
```

대표 코드:
- `CommonResponse`
- `ApiError`

## 예외 변환 위치

`GlobalExceptionHandler`가 Controller에서 올라온 예외를 HTTP 응답으로 변환합니다.

| 예외 | 응답 code | HTTP status |
| --- | --- | --- |
| `CustomException` | `ErrorCode.name()` | `ErrorCode.status` |
| `MethodArgumentNotValidException` | `VALIDATION_ERROR` | 400 |
| `ConstraintViolationException` | `VALIDATION_ERROR` | 400 |
| `IllegalArgumentException` | `VALIDATION_ERROR` | 400 |
| 기타 `Exception` | `INTERNAL_ERROR` | 500 |

Spring Security 인증 실패는 `SecurityConfig#authenticationEntryPoint`가 직접 JSON 응답을 작성합니다.

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required"
  }
}
```

## ErrorCode 목록

### 인증

| Code | Status | 의미 |
| --- | --- | --- |
| `DUPLICATE_USERNAME` | 409 | 이미 존재하는 username |
| `INVALID_CREDENTIALS` | 401 | 로그인 username/password 불일치 |
| `INVALID_TOKEN` | 401 | 유효하지 않거나 만료된 token |

### 세션

| Code | Status | 의미 |
| --- | --- | --- |
| `SESSION_ALREADY_EXISTS` | 409 | 진행 중 세션이 이미 존재 |
| `SESSION_NOT_DELETABLE` | 409 | 진행 중 세션 삭제 시도 |
| `SESSION_NOT_FOUND` | 404 | 세션 없음 또는 접근 불가 |

### 공유 세션

| Code | Status | 의미 |
| --- | --- | --- |
| `SESSION_ALREADY_SHARED` | 409 | 이미 공유된 세션 |
| `SESSION_NOT_SHARED` | 400 | 공유되지 않은 세션에 공유 기능 요청 |
| `SESSION_NOT_SHAREABLE` | 400 | 완료되지 않았거나 로그 내용이 없어 공유 불가 |
| `SESSION_NOT_IN_CREW` | 404 | 해당 크루에 공유되지 않은 세션 |
| `TAG_LIMIT_EXCEEDED` | 400 | 세션 태그 5개 초과 |

### 크루

| Code | Status | 의미 |
| --- | --- | --- |
| `CREW_NOT_FOUND` | 404 | 크루 없음 |
| `CREW_ACCESS_DENIED` | 403 | 소유자 권한 필요 |
| `NOT_CREW_MEMBER` | 403 | 크루 멤버가 아님 |
| `ALREADY_CREW_MEMBER` | 409 | 이미 가입한 크루 |
| `INVALID_INVITE_CODE` | 400 | 초대 코드가 없거나 만료됨 |
| `CREW_MEMBER_LIMIT_EXCEEDED` | 409 | 최대 인원 초과 |
| `CREW_OWNER_CANNOT_LEAVE` | 409 | 소유자 일반 탈퇴 차단 |
| `INVALID_INVITE_TTL` | 400 | 허용되지 않은 초대 코드 TTL |
| `CREW_MAX_MEMBERS_BELOW_CURRENT` | 409 | 현재 멤버 수보다 작은 최대 인원 설정 |
| `CREW_NOT_PUBLIC` | 403 | 공개 가입 불가 크루 |

### 댓글, 반응, 알림

| Code | Status | 의미 |
| --- | --- | --- |
| `INVALID_REACTION_EMOJI` | 400 | 지원하지 않는 이모지 |
| `COMMENT_NOT_FOUND` | 404 | 댓글 없음 |
| `COMMENT_ACCESS_DENIED` | 403 | 댓글 수정/삭제 권한 없음 |
| `COMMENT_PARENT_MISMATCH` | 400 | 대댓글 부모가 같은 세션이 아님 |
| `NOTIFICATION_NOT_FOUND` | 404 | 알림 없음 |
| `NOTIFICATION_ACCESS_DENIED` | 403 | 알림 소유자 아님 |

### 검색, 페이지네이션, 락, 제한

| Code | Status | 의미 |
| --- | --- | --- |
| `SEARCH_QUERY_TOO_SHORT` | 400 | 검색어 2자 미만 |
| `SEARCH_TYPE_INVALID` | 400 | 지원하지 않는 검색 타입 |
| `INVALID_CURSOR` | 400 | 커서 토큰 파싱 실패 |
| `LOCK_ACQUISITION_FAILED` | 409 | 같은 자원에 대한 다른 요청 처리 중 |
| `RATE_LIMIT_EXCEEDED` | 429 | 요청 제한 초과 |

### 공통

| Code | Status | 의미 |
| --- | --- | --- |
| `RESOURCE_NOT_FOUND` | 404 | 공통 리소스 없음 |
| `INTERNAL_ERROR` | 500 | 처리하지 못한 서버 오류 |

## 예외를 읽는 방법

1. Application 서비스에서 어떤 `CustomException`을 던지는지 확인
2. 해당 예외 클래스가 어떤 `ErrorCode`를 반환하는지 확인
3. `GlobalExceptionHandler`가 `ErrorCode.status`와 `ErrorCode.name()`으로 응답 생성
4. 인증 실패와 rate limit 실패는 일반 예외 핸들러 밖에서 직접 응답을 만들 수 있음

## 주의할 지점

- `SESSION_NOT_FOUND`는 실제 미존재뿐 아니라 다른 사용자의 세션 존재 여부를 숨기기 위해 사용될 수 있음
- `NOT_CREW_MEMBER`는 공유 세션, 댓글, 검색, 태그, presence 등 크루 접근 경계에서 반복 사용
- `LOCK_ACQUISITION_FAILED`는 비즈니스 충돌이라 409로 처리
- `RateLimitInterceptor`는 `/error` forward로 401이 되는 것을 피하려고 직접 JSON 응답을 작성
