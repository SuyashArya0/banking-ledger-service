package com.bank.ledger.application.port.out;

import com.bank.ledger.domain.model.TransactionCompletedEvent;

public interface PublishTransactionEventPort
{
    void publishTransactionCompleted(TransactionCompletedEvent event);
}