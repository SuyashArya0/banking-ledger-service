package com.bank.ledger.application.port.out;

import com.bank.ledger.domain.model.Transaction;
import java.util.Optional;

public interface SaveTransactionPort
{
    Transaction saveTransaction(Transaction transaction);
    Optional<Transaction> findByReferenceId(String referenceId);
}