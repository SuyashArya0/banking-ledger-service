// Defines the lifecycle state of a transaction
package com.bank.ledger.domain.model;

public enum TransactionStatus
{
    PENDING,
    COMPLETED,
    FAILED
}