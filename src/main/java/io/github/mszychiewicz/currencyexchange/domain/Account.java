package io.github.mszychiewicz.currencyexchange.domain;

import io.github.mszychiewicz.currencyexchange.domain.exception.InsufficientFundsException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InvalidAmountException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InvalidNameException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Account {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final Map<Currency, BigDecimal> balances;

    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Insufficient funds on account.";
    public static final String INVALID_AMOUNT_MESSAGE = "Negative amount operations are not allowed.";
    public static final String INVALID_NAME_MESSAGE = "Name must not be blank.";
    public static final Currency PLN = Currency.getInstance("PLN");

    public Account(String firstName, String lastName, BigDecimal openingBalance) {
        validateName(firstName);
        validateName(lastName);
        validateAmount(openingBalance);

        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        Map<Currency, BigDecimal> balances = new HashMap<>();
        balances.put(PLN, openingBalance);
        this.balances = balances;
    }

    void depositFunds(Currency currency, BigDecimal amount) {
        validateAmount(amount);
        balances.put(currency, balances.getOrDefault(currency, BigDecimal.ZERO).add(amount));
    }

    void withdrawFunds(Currency currency, BigDecimal amount) {
        validateAmount(amount);
        validateHasSufficientFunds(currency, amount);
        balances.put(currency, balances.get(currency).subtract(amount));
    }

    void validateHasSufficientFunds(Currency currency, BigDecimal amount) {
        if (balances.getOrDefault(currency, BigDecimal.ZERO).compareTo(amount) < 0) {
            throw new InsufficientFundsException(INSUFFICIENT_FUNDS_MESSAGE);
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new InvalidNameException(INVALID_NAME_MESSAGE);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException(INVALID_AMOUNT_MESSAGE);
        }
    }
}
