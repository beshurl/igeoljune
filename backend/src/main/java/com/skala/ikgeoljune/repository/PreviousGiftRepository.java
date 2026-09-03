package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.PreviousGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreviousGiftRepository extends JpaRepository<PreviousGift, Long> {

    List<PreviousGift> findByRecipientIdOrderByGiftedAtDescIdDesc(Long recipientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PreviousGift g where g.recipient.id in :recipientIds")
    void deleteByRecipientIds(@Param("recipientIds") List<Long> recipientIds);
}
