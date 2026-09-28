// Represents a financial movement linking two accounts with balancing journal entries
package com.bank.ledger.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Transaction
{
    private final UUID id;
    private final String referenceId; // Idempotency key
    private final UUID sourceAccountId;
    private final UUID targetAccountId;
    private final Money amount;
    private final List<JournalEntry> entries;
    private TransactionStatus status;
    private final Instant createdAt;

    public Transaction(
            UUID id,
            String referenceId,
            UUID sourceAccountId,
            UUID targetAccountId,
            Money amount,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Transaction ID must not be null");
        this.referenceId = Objects.requireNonNull(referenceId, "Reference ID must not be null");
        this.sourceAccountId = Objects.requireNonNull(sourceAccountId, "Source Account ID must not be null");
        this.targetAccountId = Objects.requireNonNull(targetAccountId, "Target Account ID must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");

        if(sourceAccountId.equals(targetAccountId))
            throw new IllegalArgumentException("Source and target accounts must be different");

        if(amount.isNegativeOrZero())
            throw new IllegalArgumentException("Transfer amount must be positive");

        this.createdAt = Objects.requireNonNullElse(createdAt, Instant.now());
        this.status = TransactionStatus.PENDING;

        // Generate the balancing debit and credit entries
        this.entries = List.of(
                new JournalEntry(UUID.randomUUID(), sourceAccountId, EntryType.DEBIT, amount, this.createdAt),
                new JournalEntry(UUID.randomUUID(), targetAccountId, EntryType.CREDIT, amount, this.createdAt)
        );
    }

    public void markCompleted()
    {
        this.status = TransactionStatus.COMPLETED;
    }

    public void markFailed()
    {
        this.status = TransactionStatus.FAILED;
    }

    // Getters
    public UUID getId() { return id; }
    public String getReferenceId() { return referenceId; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public Money getAmount() { return amount; }
    public List<JournalEntry> getEntries() { return entries; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}