// Domain Entity holding business operations for balance updates and validation.
package com.bank.ledger.domain.model;

import com.bank.ledger.domain.exception.InsufficientBalanceException;
import java.util.Objects;
import java.util.UUID;

public class Account
{
    private final UUID id;
    private final String accountNumber;
    private Money balance;
    private final Long version; // Added version field

    public Account(UUID id, String accountNumber, Money balance, Long version)
    {
        this.id = Objects.requireNonNull(id, "Account ID must not be null");
        this.accountNumber = Objects.requireNonNull(accountNumber, "Account Number must not be null");
        this.balance = Objects.requireNonNull(balance, "Balance must not be null");
        this.version = version;
    }

    public void debit(Money amount)
    {
        if(this.balance.isLessThan(amount))
            throw new InsufficientBalanceException(
                    "Account %s has insufficient balance (%s) for debit of %s"
                            .formatted(accountNumber, balance.amount(), amount.amount())
            );

        this.balance = this.balance.subtract(amount);
    }

    public void credit(Money amount)
    {
        this.balance = this.balance.add(amount);
    }

    public UUID getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public Money getBalance() { return balance; }
    public Long getVersion() { return version; }
}