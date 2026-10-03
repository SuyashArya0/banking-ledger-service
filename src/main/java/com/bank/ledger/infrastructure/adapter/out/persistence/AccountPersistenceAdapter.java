package com.bank.ledger.infrastructure.adapter.out.persistence;

import com.bank.ledger.application.port.out.LoadAccountPort;
import com.bank.ledger.application.port.out.SaveAccountPort;
import com.bank.ledger.domain.model.Account;
import com.bank.ledger.infrastructure.adapter.out.persistence.mapper.AccountMapper;
import com.bank.ledger.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort
{
    private final SpringDataTransactionRepository accountRepository;

    public AccountPersistenceAdapter(SpringDataTransactionRepository accountRepository)
    {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> loadAccount(UUID accountID)
    {
        return accountRepository.findById(accountID).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> loadAccountForUpdate(UUID accountId)
    {
        return accountRepository.findByIdWithPessimisticLock(accountId).map(AccountMapper::toDomain);
    }

    @Override
    public void saveAccount(Account account)
    {
        accountRepository.save(AccountMapper.toJpaEntity(account));
    }
}
