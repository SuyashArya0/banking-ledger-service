package com.bank.ledger.application.port.in;

public interface TransferMoneyUseCase
{
    TransferResult transfer(TransferCommand command);
}