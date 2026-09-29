package com.bank.ledger.application.port.in;

import com.bank.ledger.domain.model.Money;
import  java.util.Objects;
import java.util.UUID;

public record TransferCommand(
        String referenceId,
        UUID sourceAccountId,
        UUID targetAccountId,
        Money amount
) {
    public TransferCommand
    {
        Objects.requireNonNull(referenceId, "Reference ID is required for idempotency");
        Objects.requireNonNull(sourceAccountId, "Source Account ID is required");
        Objects.requireNonNull(targetAccountId, "Target Account ID is required");
        Objects.requireNonNull(amount, "Amount is required");

        if(sourceAccountId.equals(targetAccountId))
            throw new IllegalArgumentException("Source and target accounts must be different");
    }
}