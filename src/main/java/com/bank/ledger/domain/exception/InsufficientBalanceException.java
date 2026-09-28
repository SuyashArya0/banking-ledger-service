package com.bank.ledger.domain.exception;

public class InsufficientBalanceException extends DomainException
{
    public InsufficientBalanceException(String message)
    {
        super(message);
    }
}