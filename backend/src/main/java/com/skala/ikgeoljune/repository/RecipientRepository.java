package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    List<Recipient> findByUserId(Long userId);
}
