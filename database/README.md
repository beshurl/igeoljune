# 이걸주네? — Database

로컬 개발용 PostgreSQL 16 (Docker Compose)

## 실행

```bash
docker compose up -d
```

- DB: `ikgeoljune` / 사용자: `ikgeoljune` / 비밀번호: `ikgeoljune` / 포트: `5432`
- 최초 기동 시 `init/001_schema.sql`이 자동 실행되어 기본 테이블이 생성됩니다.
- Backend(`spring.jpa.hibernate.ddl-auto=update`)가 개발 중 스키마 변경분도 자동 반영합니다.

## 테이블 개요 (대표 흐름 UC7→UC8→UC9 중심)

| 테이블 | 대응 화면/UC |
|---|---|
| users | SCR-AUTH-001 · UC1 |
| recipients | SCR-RECIPIENT-001 · UC2 |
| gift_conditions | SCR-GIFT-001 · UC7 (budget 필수 — 예산 최우선) |
| recommendations | SCR-AI-001 · UC8·UC9 (status: pending/completed/failed) |
| recommendation_candidates | SCR-AI-001/002 · UC9~UC11 |
| gift_histories | SCR-HISTORY-001 · UC13·UC14 |

## 다음 할 일

- [ ] `init/001_schema.sql`을 기준으로 `DB.dbml` 작성 (dbdiagram.io)
- [ ] Calendar/카카오톡(선택 흐름) 관련 테이블 추가 여부 결정 후 반영
- [ ] 운영 전환 시 Flyway/Liquibase 마이그레이션 도입 검토
