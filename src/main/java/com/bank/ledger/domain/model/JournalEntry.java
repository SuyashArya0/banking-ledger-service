// Represents an individual leg of a double entry accounting transaction
package com.bank.ledger.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record JournalEntry(
        UUID id,
        UUID accountId,
        EntryType type,
        Money amount,
        Instant timestamp
) {
    public JournalEntry
    {
        Objects.requireNonNull(id, "JournalEntry ID must not be null");
        Objects.requireNonNull(accountId, "Account ID must not be null");
        Objects.requireNonNull(type, "EntryType must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(timestamp, "Timestamp must not be null");

        if(amount.isNegativeOrZero())
            throw new IllegalArgumentException("Journal entry must be greater than zero");
    }
}