# 이걸주네?

AI 기반 맞춤형 선물 추천 서비스입니다.

## 브랜치 구성

이 저장소는 브랜치별로 프로젝트를 분리해서 관리합니다.

| 브랜치 | 관리 대상 |
| --- | --- |
| `main` | 프로젝트 안내 문서와 Git 협업 규칙 |
| `igeol-front` | Vue 기반 프론트엔드 프로젝트 |
| `igeol-back` | Spring Boot 기반 백엔드 프로젝트 |

`main`에는 `README.md`와 `GitConvention.md`만 유지합니다. 프론트엔드와 백엔드 소스는 각 통합 브랜치에서 확인할 수 있습니다.

```bash
# 프론트엔드
git switch igeol-front

# 백엔드
git switch igeol-back
```

작업 브랜치 생성과 Pull Request 규칙은 `GitConvention.md`를 따릅니다.
