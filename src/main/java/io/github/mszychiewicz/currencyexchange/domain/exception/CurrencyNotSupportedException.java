package io.github.mszychiewicz.currencyexchange.domain.exception;

public class CurrencyNotSupportedException extends RuntimeException {
    public CurrencyNotSupportedException(String message) {
        super(message);
    }
}
