package com.bank.ledger.application.usecase;

import com.bank.ledger.application.port.out.*;
import com.bank.ledger.application.port.in.TransferCommand;
import com.bank.ledger.application.port.in.TransferResult;
import com.bank.ledger.application.port.in.TransferMoneyUseCase;

import com.bank.ledger.domain.exception.AccountNotFoundException;
// import com.bank.ledger.domain.exception.DuplicateTransactionException;
import com.bank.ledger.domain.model.Transaction;
import com.bank.ledger.domain.model.Account;
// import com.bank.ledger.domain.model.TransactionStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferMoneyService implements TransferMoneyUseCase
{
    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;
    private final SaveTransactionPort saveTransactionPort;

    public TransferMoneyService(
            LoadAccountPort loadAccountPort,
            SaveAccountPort saveAccountPort,
            SaveTransactionPort saveTransactionPort
    ) {
        this.loadAccountPort = Objects.requireNonNull(loadAccountPort);
        this.saveAccountPort = Objects.requireNonNull(saveAccountPort);
        this.saveTransactionPort = Objects.requireNonNull(saveTransactionPort);
    }

    @Override
    @Transactional
    public TransferResult transfer(TransferCommand command) {
        // Check idempotency key
        Optional<Transaction> existingTransaction = saveTransactionPort.findByReferenceId((command.referenceId()));

        if (existingTransaction.isPresent()) {
            Transaction tx = existingTransaction.get();
            return new TransferResult(
                    tx.getId(),
                    tx.getReferenceId(),
                    tx.getSourceAccountId(),
                    tx.getTargetAccountId(),
                    tx.getAmount(),
                    tx.getStatus(),
                    tx.getCreatedAt()
            );
        }
        // Lock accounts in deterministic order to prevent database deadlocks
        UUID firstLockId = getFirstLockId(command.sourceAccountId(), command.targetAccountId());
        UUID secondLockId = getSecondLockId(command.sourceAccountId(), command.targetAccountId());

        Account firstLocked = loadAccountPort.loadAccountForUpdate(firstLockId)
                .orElseThrow(() -> new AccountNotFoundException(firstLockId));
        Account secondLocked = loadAccountPort.loadAccountForUpdate(secondLockId)
                .orElseThrow(() -> new AccountNotFoundException(secondLockId));

        Account sourceAccount = command.sourceAccountId().equals(firstLocked.getId()) ? firstLocked : secondLocked;
        Account targetAccount = command.targetAccountId().equals(firstLocked.getId()) ? firstLocked : secondLocked;

        // Create Transaction Domain Model
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                command.referenceId(),
                sourceAccount.getId(),
                targetAccount.getId(),
                command.amount(),
                null
        );

        try {
            // Perform Domain Accounting Logic
            sourceAccount.debit(command.amount());
            targetAccount.credit(command.amount());

            // Persists updated Account Sates & Completed Transaction
            saveAccountPort.saveAccount(sourceAccount);
            saveAccountPort.saveAccount(targetAccount);

            transaction.markCompleted();
            Transaction savedTx = saveTransactionPort.saveTransaction(transaction);

            return new TransferResult(
                    savedTx.getId(),
                    savedTx.getReferenceId(),
                    savedTx.getSourceAccountId(),
                    savedTx.getTargetAccountId(),
                    savedTx.getAmount(),
                    savedTx.getStatus(),
                    savedTx.getCreatedAt()
            );
        } catch (Exception e) {
            transaction.markFailed();
            saveTransactionPort.saveTransaction(transaction);
            throw e;
        }
    }

    private UUID getFirstLockId(UUID sourceId, UUID targetId)
    {
        return sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
    }

    private UUID getSecondLockId(UUID sourceId, UUID targetId)
    {
        return sourceId.compareTo(targetId) < 0 ? targetId : sourceId;
    }
}