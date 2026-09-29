package com.bank.ledger.application.port.in;

import com.bank.ledger.domain.model.Money;
import com.bank.ledger.domain.model.TransactionStatus;
import java.time.Instant;
import java.util.UUID;

public record TransferResult(
        UUID transactionId,
        String referenceId,
        UUID sourceAccountId,
        UUID targetAccountId,
        Money amount,
        TransactionStatus status,
        Instant timestamp
) {}