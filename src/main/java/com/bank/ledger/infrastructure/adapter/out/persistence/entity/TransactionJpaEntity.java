package com.bank.ledger.infrastructure.adapter.out.persistence.entity;

import com.bank.ledger.domain.model.TransactionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity
{
    @Id
    private UUID id;

    @Column(name = "reference_id", nullable = false, unique = true, length = 64)
    private String referenceId;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TransactionJpaEntity() {}

    public TransactionJpaEntity(
            UUID id,
            String referenceId,
            UUID sourceAccountId,
            UUID targetAccountId,
            BigDecimal amount,
            String currency,
            TransactionStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.referenceId = referenceId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getReferenceId() { return referenceId; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}