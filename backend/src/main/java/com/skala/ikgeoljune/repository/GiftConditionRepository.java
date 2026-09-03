package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.GiftCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GiftConditionRepository extends JpaRepository<GiftCondition, Long> {

    @Query("select c.id from GiftCondition c where c.recipient.id in :recipientIds")
    List<Long> findIdsByRecipientIds(@Param("recipientIds") List<Long> recipientIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GiftCondition c where c.id in :conditionIds")
    void deleteByIds(@Param("conditionIds") List<Long> conditionIds);
}
