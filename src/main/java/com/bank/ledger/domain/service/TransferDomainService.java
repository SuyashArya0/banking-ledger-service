package com.bank.ledger.domain.service;

import com.bank.ledger.domain.exception.InsufficientBalanceException;
import com.bank.ledger.domain.model.Account;
import com.bank.ledger.domain.model.Money;

public class TransferDomainService
{
    // Validates domain constraints before attempting a transfer across two accounts
    public void validateTransfer(Account sourceAccount, Account targetAccount, Money amount)
    {
        if(sourceAccount.getId().equals(targetAccount.getId()))
            throw new IllegalArgumentException("Cannot transfer money to same account");

        if(amount.isNegativeOrZero())
            throw new IllegalArgumentException("Transfer amount must be strictly positive");

        if(sourceAccount.getBalance().isLessThan(amount))
            throw new InsufficientBalanceException(
                    "Source account %s balance (%s) is insufficient for transfer of %s"
                            .formatted(sourceAccount.getAccountNumber(), sourceAccount.getBalance().amount(), amount.amount())
            );
    }
}
