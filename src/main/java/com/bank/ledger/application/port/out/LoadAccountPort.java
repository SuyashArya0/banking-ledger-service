package com.bank.ledger.application.port.out;

import com.bank.ledger.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

public interface LoadAccountPort
{
    Optional<Account> loadAccount(UUID accountId);
    // Loads an account using pessimistic locking to prevent race condition
    Optional<Account> loadAccountForUpdate(UUID accountId);
}