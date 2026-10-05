package com.bank.ledger.infrastructure.adapter.out.persistence.repository;

import com.bank.ledger.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    Optional<TransactionJpaEntity> findByReferenceId(String referenceId);
}