package com.bank.ledger.domain.exception;

public class DuplicateTransactionException extends DomainException
{
    public DuplicateTransactionException(String referenceId)
    {
        super("Transaction with reference ID '%s' has already been processed".formatted(referenceId));
    }
}