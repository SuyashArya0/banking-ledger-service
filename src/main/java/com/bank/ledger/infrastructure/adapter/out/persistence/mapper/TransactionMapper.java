package com.bank.ledger.infrastructure.adapter.out.persistence.mapper;

import com.bank.ledger.domain.model.Money;
import com.bank.ledger.domain.model.Transaction;
import com.bank.ledger.domain.model.TransactionStatus;
import com.bank.ledger.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;

import java.util.Currency;

public class TransactionMapper
{
    public static Transaction toDomain(TransactionJpaEntity entity)
    {
        if(entity == null) return null;

        Money money = new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency()));
        Transaction tx = new Transaction(
                entity.getId(),
                entity.getReferenceId(),
                entity.getSourceAccountId(),
                entity.getTargetAccountId(),
                money,
                entity.getCreatedAt()
        );

        if(entity.getStatus() == TransactionStatus.COMPLETED)
            tx.markCompleted();
        else if(entity.getStatus() == TransactionStatus.FAILED)
            tx.markFailed();

        return tx;
    }

    public static TransactionJpaEntity toJpaEntity(Transaction domain)
    {
        if(domain == null) return null;

        return new TransactionJpaEntity(
                domain.getId(),
                domain.getReferenceId(),
                domain.getSourceAccountId(),
                domain.getTargetAccountId(),
                domain.getAmount().amount(),
                domain.getAmount().currency().getCurrencyCode(),
                domain.getStatus(),
                domain.getCreatedAt()
        );
    }
}