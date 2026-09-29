package com.bank.ledger.application.port.in;

import com.bank.ledger.domain.model.Money;
import java.util.UUID;

public interface GetAccountBalanceQuery
{
    Money getBalance(UUID accountId);
}