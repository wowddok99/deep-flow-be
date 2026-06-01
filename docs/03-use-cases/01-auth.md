# Auth Use Cases

## 회원가입과 로그인

### 목적

사용자 계정을 생성하고 JWT access token과 refresh token으로 인증합니다.

### 흐름 요약

```text
AuthController
→ AuthService
→ UserRepository
→ TokenProvider
→ User refresh token 저장
```

### API

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/reissue`
- `POST /api/v1/auth/logout`

### Application

- `AuthService#signup`
- `AuthService#login`
- `AuthService#reissue`
- `AuthService#logout`

### Domain

- `User`
- `Role`

### Infra

- `UserRepositoryImpl`
- `JwtProvider`

### 주의점

- 로그인 시 refresh token은 사용자 엔티티에 저장
- 인증이 필요한 API는 `JwtAuthenticationFilter`가 `CustomUserDetails`를 SecurityContext에 넣어야 동작
- refresh token 재발급과 로그아웃은 저장된 refresh token과 요청 token의 일치 여부가 중요

### 관련 코드

- API: `AuthController`
- Application: `AuthService`
- Domain: `User`, `Role`
- Persistence: `UserRepository`
- Token: `TokenProvider`, `JwtProvider`

### 문서 반영 기준

- 회원가입, 로그인, 토큰 재발급, 로그아웃 요청 흐름이 바뀌는 경우
- refresh token 저장 방식이나 검증 방식이 바뀌는 경우
- JWT claim, 만료시간, 쿠키 정책이 바뀌는 경우
- 인증 실패 응답이나 인증 필터 동작이 바뀌는 경우
