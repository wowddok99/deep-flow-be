# Media Use Cases

## 이미지 업로드

### 목적

집중 로그에 사용할 이미지를 업로드하고 URL을 반환합니다.

### 흐름 요약

```text
POST /api/v1/images
→ ImageController#upload
→ ImageService#upload
→ ImageStorage#upload
→ R2ImageStorage
```

### 주의점

- multipart 요청 사용
- 로그 수정에서 제거된 이미지는 `ImageService#deleteRemovedImages`로 삭제
- R2 설정은 환경변수에 의존

### 관련 코드

- API: `ImageController`
- Application: `ImageService`
- Port: `ImageStorage`
- Infra: `R2ImageStorage`

### 문서 반영 기준

- 업로드 응답 형식이 바뀌는 경우
- 이미지 저장 위치나 외부 스토리지가 바뀌는 경우
- 로그 수정에서 이미지 삭제 정책이 바뀌는 경우
