package com.bank.ledger.domain.exception;

import java.util.UUID;

public class AccountNotFoundException extends DomainException
{
    public AccountNotFoundException(UUID accountId)
    {
        super("Account not found with ID: " + accountId);
    }

    public AccountNotFoundException(String accountNumber)
    {
        super("Account not found with account number: " + accountNumber);
    }
}