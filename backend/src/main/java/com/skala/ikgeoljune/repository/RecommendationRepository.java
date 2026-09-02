package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
