package com.bank.ledger.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity
{
    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 34)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Version
    private Long version;

    public AccountJpaEntity() {}

    public AccountJpaEntity(UUID id, String accountNumber, BigDecimal balance, String currency, Long version)
    {
        this.id = id; // Objects.requireNonNull()
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.version = version;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}