package io.github.mszychiewicz.currencyexchange.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Currency;

@RequiredArgsConstructor
public enum SupportedCurrencies {
    USD;

    static boolean isNotSupported(Currency currency) {
        String currencyCode = currency.getCurrencyCode();
        return Arrays.stream(values()).noneMatch(v -> v.toString().equals(currencyCode));
    }
}
