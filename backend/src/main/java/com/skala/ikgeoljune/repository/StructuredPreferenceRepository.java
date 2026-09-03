package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;
import com.skala.ikgeoljune.domain.StructuredPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StructuredPreferenceRepository extends JpaRepository<StructuredPreference, Long> {

    List<StructuredPreference> findByRecipientIdOrderByIdAsc(Long recipientId);

    /** PREF-003 preferenceType / sourceType 선택 필터 */
    @Query("""
            select p from StructuredPreference p
            where p.recipient.id = :recipientId
              and (:preferenceType is null or p.preferenceType = :preferenceType)
              and (:sourceType is null or p.sourceType = :sourceType)
            order by p.id asc
            """)
    List<StructuredPreference> search(@Param("recipientId") Long recipientId,
                                      @Param("preferenceType") PreferenceType preferenceType,
                                      @Param("sourceType") SourceType sourceType);

    boolean existsByRecipientIdAndPreferenceTypeAndPreferenceValue(Long recipientId,
                                                                   PreferenceType preferenceType,
                                                                   String preferenceValue);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StructuredPreference p where p.recipient.id in :recipientIds")
    void deleteByRecipientIds(@Param("recipientIds") List<Long> recipientIds);
}
