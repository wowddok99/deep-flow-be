# Crew Use Cases

## 크루 생성

### 목적

새 크루를 만들고 생성자를 소유자 멤버로 등록합니다.

### 흐름 요약

```text
POST /api/v1/crews
→ CrewController#create
→ CrewService#create
→ UserRepository#findById
→ Crew#create
→ CrewRepository#save
→ CrewMember#newOwner
→ CrewMemberRepository#save
```

### 주의점

- 크루 생성과 소유자 멤버 등록은 하나의 트랜잭션으로 처리
- 공개 여부가 없으면 기본값은 private
- 최대 인원은 서비스 규칙에 따라 정규화

### 관련 코드

- API: `CrewController`
- Application: `CrewService#create`
- Domain: `Crew`, `CrewMember`, `Visibility`, `CrewRole`
- Persistence: `CrewRepository`, `CrewMemberRepository`

### 갱신 기준

- 크루 생성 필수값이나 기본값이 바뀌는 경우
- 생성 후 소유자 등록 방식이 바뀌는 경우
- 공개/비공개 기본 정책이 바뀌는 경우

## 크루 가입

### 목적

초대 코드 또는 공개 크루 경로로 크루에 가입합니다.

### 흐름 요약

```text
POST /api/v1/crews/join 또는 POST /api/v1/crews/{crewId}/join
→ CrewController
→ CrewService#joinByCode 또는 CrewService#joinPublic
→ CrewJoinLocker#join
→ CrewService#joinCrewLockedInternal
→ CrewMemberRepository#existsByCrewIdAndUserId
→ CrewMemberRepository#countByCrewId
→ CrewMember#newMember
```

### 주의점

- 초대 코드 가입은 코드 유효성만 먼저 확인하고 실제 가입은 크루별 락 안으로 위임
- 공개 가입은 공개 여부와 최대 인원 검증을 락 안에서 처리
- `joinByCode`, `joinPublic`은 `Propagation.NOT_SUPPORTED`로 외부 트랜잭션을 열지 않음
- 최대 인원 초과와 중복 가입은 락 내부에서 검증해야 함

### 관련 코드

- API: `CrewController`
- Application: `CrewService#joinByCode`, `CrewService#joinPublic`, `CrewJoinLocker`, `CrewService#joinCrewLockedInternal`
- Domain: `Crew`, `CrewMember`, `CrewRole`, `Visibility`
- Persistence: `CrewRepository`, `CrewMemberRepository`
- Lock: `DistributedLock`, `DistributedLockAop`

### 갱신 기준

- 초대 코드 가입 조건이 바뀌는 경우
- 공개 크루 가입 정책이 바뀌는 경우
- 멤버 정원 검증 방식이 바뀌는 경우
- 락 범위나 트랜잭션 경계가 바뀌는 경우

## 공유 세션 피드 조회

### 목적

크루에 공유된 세션을 커서 기반으로 조회하고, 태그 필터를 적용할 수 있습니다.

### 흐름 요약

```text
GET /api/v1/crews/{crewId}/feed
→ CrewFeedController#getFeed
→ CrewFeedService#getFeed
→ CrewMemberRepository membership 확인
→ SessionRepository 공유 세션 조회
→ SessionTagRepository 태그 조회
→ 댓글과 반응 수 조립
```

### 주의점

- 크루 멤버만 피드를 조회할 수 있음
- 커서 토큰은 `SharedFeedCursor`로 인코딩/디코딩
- 태그 필터는 공유 세션 태그 기준으로 동작

### 관련 코드

- API: `CrewFeedController`
- Application: `CrewFeedService`
- Domain: `FocusSession`, `SessionTag`
- Persistence: `SessionRepository`, `SessionTagRepository`, `CrewMemberRepository`

### 문서 반영 기준

- 피드 정렬, 커서, 필터 규칙이 바뀌는 경우
- 공유 세션 노출 조건이 바뀌는 경우
- 응답 필드나 집계 기준이 바뀌는 경우
