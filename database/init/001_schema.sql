-- 이걸주네? 초기 스키마
-- Backend 엔티티(src/main/java/.../domain)와 1:1 대응, snake_case 컬럼명 사용
-- 참고: 개발 중에는 backend의 jpa.hibernate.ddl-auto=update가 스키마를 자동 반영하므로
--       이 스크립트는 (1) DB.dbml 작성 시 기준 문서, (2) docker-compose 최초 기동 시 시드 용도로 사용합니다.

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    google_sub  VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(100),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- SCR-RECIPIENT-001 · UC2
CREATE TABLE IF NOT EXISTS recipients (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL REFERENCES users(id),
    name                  VARCHAR(100) NOT NULL,
    relationship          VARCHAR(50),
    age                   INT,
    gender                VARCHAR(20),
    upcoming_anniversary  DATE,
    exclude_tags          TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_recipients_user_id ON recipients(user_id);

-- SCR-GIFT-001 · UC7 (대표 흐름) — budget은 필수(예산 최우선 원칙)
CREATE TABLE IF NOT EXISTS gift_conditions (
    id                BIGSERIAL PRIMARY KEY,
    recipient_id      BIGINT NOT NULL REFERENCES recipients(id),
    budget            INT NOT NULL,
    anniversary_date  DATE,
    preference_tags   TEXT,
    exclude_tags      TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_gift_conditions_recipient_id ON gift_conditions(recipient_id);

-- SCR-AI-001 ★핵심 · UC8(AI 확장 지점)~UC9
-- status: pending/completed/failed (AI 비동기 파이프라인 표현)
CREATE TABLE IF NOT EXISTS recommendations (
    id                          BIGSERIAL PRIMARY KEY,
    gift_condition_id           BIGINT NOT NULL REFERENCES gift_conditions(id),
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                 CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    previous_recommendation_id  BIGINT REFERENCES recommendations(id),
    failure_reason              TEXT,
    created_at                  TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_recommendations_gift_condition_id ON recommendations(gift_condition_id);

-- SCR-AI-001/002 · UC9~UC11 추천 후보 + 피드백
CREATE TABLE IF NOT EXISTS recommendation_candidates (
    id                BIGSERIAL PRIMARY KEY,
    recommendation_id BIGINT NOT NULL REFERENCES recommendations(id),
    name              VARCHAR(200) NOT NULL,
    price             INT NOT NULL,
    reason            TEXT,
    image_url         VARCHAR(500),
    liked             BOOLEAN,
    dislike_reason    VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_recommendation_candidates_recommendation_id
    ON recommendation_candidates(recommendation_id);

-- SCR-HISTORY-001 · UC13·UC14
CREATE TABLE IF NOT EXISTS gift_histories (
    id                            BIGSERIAL PRIMARY KEY,
    recipient_id                  BIGINT NOT NULL REFERENCES recipients(id),
    recommendation_candidate_id   BIGINT REFERENCES recommendation_candidates(id),
    occasion                      VARCHAR(100),
    gift_name                     VARCHAR(200) NOT NULL,
    confirmed_date                DATE,
    created_at                    TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_gift_histories_recipient_id ON gift_histories(recipient_id);
