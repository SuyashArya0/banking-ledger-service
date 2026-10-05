package com.bank.ledger.infrastructure.adapter.out.messaging;

import com.bank.ledger.application.port.out.PublishTransactionEventPort;
import com.bank.ledger.domain.model.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionEventAdapter implements PublishTransactionEventPort
{
    private static final Logger log = LoggerFactory.getLogger(KafkaTransactionEventAdapter.class);
    private final ApplicationEventPublisher eventPublisher;

    public KafkaTransactionEventAdapter(ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishTransactionCompleted(TransactionCompletedEvent event)
    {
        log.info("Publishing completed transaction event for reference: {}", event.referenceId());
        eventPublisher.publishEvent(event);
    }
}