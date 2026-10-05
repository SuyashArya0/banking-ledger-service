package com.bank.ledger.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotBlank(message = "Reference ID is required")
        String referenceId,

        @NotNull(message = "Source account ID is required")
        UUID sourceAccountId,

        @NotNull(message = "Target account ID is required")
        UUID targetAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency code is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be valid 3-letter ISO code")
        String currency
) {}