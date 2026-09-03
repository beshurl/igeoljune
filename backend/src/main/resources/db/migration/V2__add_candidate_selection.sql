-- 최종 선물 선택 (UC 최종 단계).
-- 추천 후보 하나를 선택하면 selected_at 에 선택 시각을 기록한다.
-- 한 추천 실행 안에서는 최대 1건만 선택할 수 있다.

ALTER TABLE recommendation_candidates ADD COLUMN selected_at TIMESTAMPTZ;

CREATE UNIQUE INDEX uq_candidate_selected_per_recommendation
    ON recommendation_candidates (recommendation_id)
    WHERE selected_at IS NOT NULL;
