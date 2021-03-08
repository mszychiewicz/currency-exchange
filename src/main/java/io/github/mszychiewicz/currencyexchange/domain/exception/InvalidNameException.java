package io.github.mszychiewicz.currencyexchange.domain.exception;

public class InvalidNameException extends RuntimeException {
    public InvalidNameException(String message) {
        super(message);
    }
}
