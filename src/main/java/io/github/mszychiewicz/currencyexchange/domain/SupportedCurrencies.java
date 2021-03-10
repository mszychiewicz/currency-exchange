package io.github.mszychiewicz.currencyexchange.domain;

import io.github.mszychiewicz.currencyexchange.domain.exception.CurrencyNotSupportedException;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Currency;

@RequiredArgsConstructor
public enum SupportedCurrencies {
    USD;

    public static final String CURRENCY_NOT_SUPPORTED_MESSAGE = "Currency not supported.";

    static public void validateCurrencySupport(Currency currency) {
        if (isNotInValues(currency.getCurrencyCode())) {
            throw new CurrencyNotSupportedException(CURRENCY_NOT_SUPPORTED_MESSAGE);
        }
    }

    static private boolean isNotInValues(String currencyCode) {
        return Arrays.stream(values()).noneMatch(v -> v.toString().equals(currencyCode));
    }
}
