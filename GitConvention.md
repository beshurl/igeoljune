# Git Convention

## 1. 브랜치 구성

하나의 저장소에서 문서, 프론트엔드, 백엔드를 브랜치별로 분리한다.

```text
main         문서 전용 브랜치
igeol-front  프론트엔드 통합 브랜치
igeol-back   백엔드 통합 브랜치
```

### main

- `README.md`, `GitConvention.md`만 관리한다.
- 프론트엔드와 백엔드 소스 코드를 병합하지 않는다.
- 문서 변경은 `main`에서 작업 브랜치를 생성한 뒤 Pull Request로 반영한다.
- 직접 Push하지 않는다.

### igeol-front

- `frontend/` 프로젝트만 관리한다.
- 프론트엔드 작업 브랜치는 `igeol-front`에서 생성한다.
- 작업 완료 후 `igeol-front`를 대상으로 Pull Request를 생성한다.
- `main`이나 `igeol-back`으로 병합하지 않는다.

### igeol-back

- `backend/` 프로젝트만 관리한다.
- 백엔드 작업 브랜치는 `igeol-back`에서 생성한다.
- 작업 완료 후 `igeol-back`을 대상으로 Pull Request를 생성한다.
- `main`이나 `igeol-front`로 병합하지 않는다.

---

## 2. 브랜치 이름

작업 브랜치는 다음 형식을 사용한다.

```text
{type}/{scope}/{feature-name}
```

### Type

| Type | 설명 |
| --- | --- |
| `feat` | 새로운 기능 개발 |
| `fix` | 버그 수정 |
| `refac` | 기능 변경 없는 코드 구조 개선 |
| `design` | UI 및 스타일 수정 |
| `test` | 테스트 코드 작성 및 수정 |
| `docs` | 문서 작성 및 수정 |
| `chore` | 설정, 패키지, 빌드 등 기타 작업 |
| `hotfix` | 긴급 수정 |

### Scope

| Scope | 설명 | 기준 브랜치 | 병합 대상 |
| --- | --- | --- | --- |
| `front` | 프론트엔드 작업 | `igeol-front` | `igeol-front` |
| `back` | 백엔드 작업 | `igeol-back` | `igeol-back` |
| `common` | 공통 문서 작업 | `main` | `main` |

### 예시

```text
feat/front/gift-recommendation
fix/front/login-redirect
design/front/recommendation-page

feat/back/gift-recommendation
fix/back/recommendation-response
test/back/recommendation-service

docs/common/api-spec
chore/common/repository-structure
```

---

## 3. 작업 흐름

### 프론트엔드

```bash
git switch igeol-front
git pull origin igeol-front
git switch -c feat/front/gift-recommendation
```

```text
feat/front/*
fix/front/*
design/front/*
refac/front/*
        ↓ Pull Request
    igeol-front
```

### 백엔드

```bash
git switch igeol-back
git pull origin igeol-back
git switch -c feat/back/gift-recommendation
```

```text
feat/back/*
fix/back/*
refac/back/*
test/back/*
       ↓ Pull Request
     igeol-back
```

### 공통 문서

```bash
git switch main
git pull origin main
git switch -c docs/common/update-guide
```

```text
docs/common/*
chore/common/*
       ↓ Pull Request
        main
```

`igeol-front`와 `igeol-back`은 서로 병합하지 않으며, 두 브랜치를 `main`에도 병합하지 않는다.

---

## 4. 커밋 메시지

커밋 메시지는 다음 형식을 사용한다.

```text
type(scope): 작업 내용
```

### 예시

```text
feat(front): 선물 추천 조건 입력 기능 구현
design(front): 선물 추천 카드 UI 수정
fix(front): 로그인 리다이렉트 오류 수정

feat(back): 선물 추천 API 구현
fix(back): 추천 결과 중복 저장 오류 수정
test(back): 선물 추천 서비스 테스트 추가

docs(common): 프로젝트 실행 방법 수정
chore(common): 브랜치별 프로젝트 구조 분리
```

### 작성 규칙

- 작업 내용은 한글로 작성한다.
- 문장 끝에 마침표를 사용하지 않는다.
- 하나의 커밋에는 하나의 목적만 포함한다.
- 변경 내용을 구체적으로 작성한다.

```text
X feat(front): 기능 추가
X fix(back): 오류 수정

O feat(front): 선물 추천 결과 페이지 구현
O fix(back): 추천 결과 중복 저장 오류 수정
```

---

## 5. 병합 규칙

- 모든 변경은 Pull Request를 통해 통합 브랜치에 반영한다.
- 작업 브랜치의 대상 브랜치를 정확하게 지정한다.
- 작업 브랜치를 통합 브랜치에 반영할 때는 Squash Merge를 기본으로 한다.
- 병합 전 변경 파일에 다른 프로젝트가 포함되지 않았는지 확인한다.

```text
Frontend 작업 → igeol-front
Backend 작업  → igeol-back
문서 작업     → main
```

---

## 6. 최종 구조

```text
main
├── README.md
└── GitConvention.md

igeol-front
└── frontend/

igeol-back
└── backend/
```
