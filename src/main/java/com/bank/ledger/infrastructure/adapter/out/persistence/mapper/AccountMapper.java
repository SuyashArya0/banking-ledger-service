package com.bank.ledger.infrastructure.adapter.out.persistence.mapper;

import com.bank.ledger.domain.model.Account;
import com.bank.ledger.domain.model.Money;
import com.bank.ledger.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;

import java.util.Currency;

public class AccountMapper
{
    public static Account toDomain(AccountJpaEntity entity)
    {
        if(entity == null) return null;

        Money balance = new Money(entity.getBalance(), Currency.getInstance(entity.getCurrency()));
        return new Account(entity.getId(), entity.getAccountNumber(), balance);
    }

    public static AccountJpaEntity toJpaEntity(Account domain)
    {
        if(domain == null) return null;

        return new AccountJpaEntity(
                domain.getId(),
                domain.getAccountNumber(),
                domain.getBalance().amount(),
                domain.getBalance().currency().getCurrencyCode()
        );
    }
}