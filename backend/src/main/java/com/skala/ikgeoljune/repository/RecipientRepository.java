package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    List<Recipient> findByUserIdOrderByIdDesc(Long userId);

    @Query("select r.id from Recipient r where r.user.id = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Recipient r where r.id in :recipientIds")
    void deleteByIds(@Param("recipientIds") List<Long> recipientIds);
}
