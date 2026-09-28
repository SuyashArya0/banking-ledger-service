// An immutable Value Object enforcing Banker's Rounding (HALF_EVEN) and strict currency matching.
package com.bank.ledger.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency)
{
    public Money
    {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(String amount, String currencyCode)
    {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public Money add(Money other)
    {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isGreaterThan(Money other)
    {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other)
    {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isNegativeOrZero(Money other)
    {
        return this.amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    private void ensureSameCurrency(Money other)
    {
        if(!this.currency.equals(other.currency()))
            throw new IllegalArgumentException(
                    "Currency Mismatch: Cannot operate on %s and %s"
                            .formatted(this.currency, other.currency())
            );
    }
}