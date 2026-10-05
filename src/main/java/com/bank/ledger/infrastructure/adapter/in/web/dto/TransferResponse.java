package com.bank.ledger.infrastructure.adapter.in.web.dto;

import com.bank.ledger.domain.model.TransactionStatus;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        UUID transactionId,
        String referenceId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        Instant timestamp
) {}