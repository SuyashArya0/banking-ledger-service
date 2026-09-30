package com.bank.ledger.application.port.out;

import com.bank.ledger.domain.model.Account;

public interface SaveAccountPort
{
    void saveAccount(Account account);
}