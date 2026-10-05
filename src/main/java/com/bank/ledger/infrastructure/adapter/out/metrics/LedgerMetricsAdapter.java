package com.bank.ledger.infrastructure.adapter.out.metrics;

import com.bank.ledger.application.port.out.RecordMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LedgerMetricsAdapter implements RecordMetricsPort
{
    private final Counter successfulTransferCounter;
    private final Counter failedTransferCounter;
    private final MeterRegistry meterRegistry;

    public LedgerMetricsAdapter(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
        this.successfulTransferCounter = Counter.builder("ledger.transfers.completed")
                .description("Total number of successful ledger transfers")
                .register(meterRegistry);

        this.failedTransferCounter = Counter.builder("ledger.transfers.failed")
                .description("Total number of failed ledger transfers")
                .register(meterRegistry);
    }

    @Override
    public void recordSuccess(BigDecimal amount, String currency)
    {
        successfulTransferCounter.increment();
        meterRegistry.summary("ledger.transfers.volume", "currency", currency)
                .record(amount.doubleValue());
    }

    @Override
    public void recordFailure()
    {
        failedTransferCounter.increment();
    }
}
