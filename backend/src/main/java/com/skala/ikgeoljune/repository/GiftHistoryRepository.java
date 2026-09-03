package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.GiftHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftHistoryRepository extends JpaRepository<GiftHistory, Long> {
    List<GiftHistory> findByRecipientId(Long recipientId);
}
