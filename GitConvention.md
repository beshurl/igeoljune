# Git Convention

## 1. Branch Strategy

하나의 Repository에서 Frontend와 Backend를 함께 관리한다.

기본 브랜치는 다음과 같이 구성한다.

```text
main
├── igeol-front
└── igeol-back
```

### main

* Frontend와 Backend가 최종적으로 통합되는 브랜치
* 배포 가능한 안정적인 코드만 유지
* 직접 Push 금지
* `igeol-front`, `igeol-back`에서 Pull Request를 통해 Merge

### igeol-front

* Frontend 개발 통합 브랜치
* Frontend 작업 브랜치는 `igeol-front`에서 생성
* 작업 완료 후 `igeol-front`로 Pull Request 생성

### igeol-back

* Backend 개발 통합 브랜치
* Backend 작업 브랜치는 `igeol-back`에서 생성
* 작업 완료 후 `igeol-back`으로 Pull Request 생성

---

## 2. Branch Naming

작업 브랜치는 다음 형식을 사용한다.

```text
{type}/{scope}/{feature-name}
```

### Type

| Type     | 설명                  |
| -------- | ------------------- |
| `feat`   | 새로운 기능 개발           |
| `fix`    | 버그 수정               |
| `refac`  | 코드 리팩터링             |
| `design` | UI / 스타일 수정         |
| `test`   | 테스트 코드 작성 및 수정      |
| `docs`   | 문서 작성 및 수정          |
| `chore`  | 설정, 패키지, 빌드 등 기타 작업 |
| `hotfix` | 긴급 수정               |

### Scope

| Scope    | 설명          |
| -------- | ----------- |
| `front`  | Frontend 작업 |
| `back`   | Backend 작업  |
| `common` | 프로젝트 공통 작업  |

### Frontend 예시

```text
feat/front/gift-recommendation
feat/front/google-login
design/front/recommendation-page
fix/front/login-redirect
refac/front/gift-form
```

### Backend 예시

```text
feat/back/gift-recommendation
feat/back/google-oauth
fix/back/recommendation-response
refac/back/gift-service
test/back/recommendation-service
```

### 공통 작업 예시

```text
docs/common/api-spec
chore/common/docker
chore/common/github-actions
```

Frontend 작업 브랜치는 `igeol-front`에서 생성하고, Backend 작업 브랜치는 `igeol-back`에서 생성한다.

---

## 3. Branch Flow

### Frontend

```text
feat/front/*
fix/front/*
refac/front/*
      ↓
  igeol-front
      ↓
     main
```

### Backend

```text
feat/back/*
fix/back/*
refac/back/*
      ↓
  igeol-back
      ↓
     main
```

### Frontend 작업 시작

```bash
git checkout igeol-front
git pull origin igeol-front
git checkout -b feat/front/gift-recommendation
```

### Backend 작업 시작

```bash
git checkout igeol-back
git pull origin igeol-back
git checkout -b feat/back/gift-recommendation
```

작업 완료 후 각 개발 통합 브랜치로 Pull Request를 생성한다.

```text
feat/front/* → igeol-front
feat/back/*  → igeol-back
```

Frontend 또는 Backend의 배포 단위 개발이 완료되면 `main`으로 Pull Request를 생성한다.

```text
igeol-front ─┐
             ├──→ main
igeol-back ──┘
```

---

## 4. Commit Convention

Commit Message는 다음 형식을 사용한다.

```text
type(scope): 작업 내용
```

예시:

```text
feat(front): 선물 추천 조건 입력 기능 구현
feat(back): 선물 추천 API 구현

fix(front): 로그인 리다이렉트 오류 수정
fix(back): 추천 결과 저장 오류 수정
```

---

## 5. Commit Type

| Type     | 설명                   |
| -------- | -------------------- |
| `feat`   | 새로운 기능 추가            |
| `fix`    | 버그 수정                |
| `refac`  | 기능 변경 없는 코드 구조 개선    |
| `design` | UI / CSS 수정          |
| `style`  | 코드 포맷팅 등 로직 변경 없는 수정 |
| `docs`   | 문서 작성 및 수정           |
| `test`   | 테스트 코드 작성 및 수정       |
| `chore`  | 환경 설정, 패키지, 빌드 관련 작업 |
| `perf`   | 성능 개선                |

---

## 6. Commit Scope

| Scope    | 설명          |
| -------- | ----------- |
| `front`  | Frontend 작업 |
| `back`   | Backend 작업  |
| `common` | 프로젝트 공통 작업  |

---

## 7. Commit Example

### Frontend

```text
feat(front): 선물 추천 조건 입력 폼 구현
feat(front): 추천 결과 API 연동
design(front): 선물 추천 카드 UI 구현
fix(front): 로그인 리다이렉트 오류 수정
refac(front): 추천 폼 상태 관리 로직 분리
```

### Backend

```text
feat(back): 선물 추천 API 구현
feat(back): 구글 OAuth 로그인 구현
fix(back): 추천 결과 저장 오류 수정
refac(back): 추천 서비스 로직 분리
test(back): 선물 추천 서비스 테스트 추가
```

### Common

```text
docs(common): API 명세 작성
chore(common): Docker 설정 추가
chore(common): GitHub Actions 설정
```

---

## 8. Commit 작성 규칙

### 하나의 Commit에는 하나의 작업만 포함한다.

좋은 예:

```text
feat(back): 선물 추천 API 구현
```

```text
fix(front): 추천 결과 렌더링 오류 수정
```

좋지 않은 예:

```text
feat(front): 추천 페이지 구현 및 로그인 수정 및 CSS 변경
```

### 작업 내용을 명확하게 작성한다.

좋은 예:

```text
feat(front): 선물 추천 조건 입력 기능 구현
fix(back): 추천 결과 중복 저장 오류 수정
```

좋지 않은 예:

```text
feat(front): 기능 추가
fix(back): 수정
```

### Commit Message 규칙

* 작업 내용은 한글로 작성
* 문장 끝에 마침표를 사용하지 않음
* 무엇을 변경했는지 명확하게 작성
* 하나의 Commit에는 하나의 목적만 포함
* 의미 없는 Commit Message 사용 금지

```text
X feat(front): 수정
X fix(back): 오류 수정
X chore(common): 작업

O feat(front): 선물 추천 결과 페이지 구현
O fix(back): 추천 결과가 중복 저장되는 오류 수정
O refac(front): 추천 API 호출 로직 분리
```

---

## 9. Merge Rule

작업 브랜치는 반드시 Pull Request를 통해 각 개발 통합 브랜치에 Merge한다.

```text
feat/front/* → igeol-front
fix/front/*  → igeol-front

feat/back/*  → igeol-back
fix/back/*   → igeol-back
```

Frontend / Backend 개발이 완료된 경우 각각 `main`으로 Pull Request를 생성한다.

```text
igeol-front → main
igeol-back  → main
```

작업 브랜치를 개발 통합 브랜치에 Merge할 때는 **Squash Merge**를 기본으로 사용한다.

예:

```text
feat/front/gift-recommendation
              ↓
          igeol-front
```

여러 개의 작업 Commit을 하나의 의미 있는 Commit으로 정리한다.

```text
feat(front): 선물 추천 결과 페이지 구현
```

---

## 10. Branch Summary

```text
main
│
├── igeol-front
│   ├── feat/front/*
│   ├── fix/front/*
│   ├── refac/front/*
│   └── design/front/*
│
└── igeol-back
    ├── feat/back/*
    ├── fix/back/*
    ├── refac/back/*
    └── test/back/*
```

### 핵심 규칙

```text
Branch

feat/front/* → igeol-front → main
feat/back/*  → igeol-back  → main


Commit

feat(front): 작업 내용
feat(back): 작업 내용

fix(front): 작업 내용
fix(back): 작업 내용
```
