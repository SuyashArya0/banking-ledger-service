package com.bank.ledger.infrastructure.adapter.out.persistence;

import com.bank.ledger.infrastructure.adapter.out.persistence.mapper.TransactionMapper;
import com.bank.ledger.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.bank.ledger.application.port.out.SaveTransactionPort;
import com.bank.ledger.domain.model.Transaction;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransactionPersistenceAdapter implements SaveTransactionPort
{
    private final SpringDataTransactionRepository transactionRepository;

    public TransactionPersistenceAdapter(SpringDataTransactionRepository transactionRepository)
    {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction saveTransaction(Transaction transaction)
    {
        var entity = transactionRepository.save(TransactionMapper.toJpaEntity(transaction));

        return TransactionMapper.toDomain(entity);
    }

    @Override
    public Optional<Transaction> findByReferenceId(String referenceId)
    {
        return transactionRepository.findByReferenceId(referenceId).map(TransactionMapper::toDomain);
    }
}