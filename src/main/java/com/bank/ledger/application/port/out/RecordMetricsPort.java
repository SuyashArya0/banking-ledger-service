package com.bank.ledger.application.port.out;

import java.math.BigDecimal;

public interface RecordMetricsPort
{
    void recordSuccess(BigDecimal amount, String currency);
    void recordFailure();
}
