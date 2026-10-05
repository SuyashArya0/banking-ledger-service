package com.bank.ledger.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TransactionCompletedEvent(
        UUID transactionId,
        String referenceId,
        UUID sourceAccountId,
        UUID targetAccountId,
        Money amount,
        Instant occurredAt
) {
    public TransactionCompletedEvent
    {
        Objects.requireNonNull(transactionId, "Transaction ID must not be null");
        Objects.requireNonNull(referenceId, "Reference ID must not be null");
        Objects.requireNonNull(sourceAccountId, "Source account ID must not be null");
        Objects.requireNonNull(targetAccountId, "Target account ID must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(occurredAt, "OccurredAt timestamp must not be null");
    }
}