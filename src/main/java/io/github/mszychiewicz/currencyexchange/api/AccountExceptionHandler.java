package io.github.mszychiewicz.currencyexchange.api;

import io.github.mszychiewicz.currencyexchange.domain.exception.AccountNotFoundException;
import io.github.mszychiewicz.currencyexchange.domain.exception.CurrencyNotSupportedException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
class AccountExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFoundException(
            AccountNotFoundException ex) {
        String body = ex.getMessage();
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handleInsufficientFundsException(
            InsufficientFundsException ex) {
        String body = ex.getMessage();
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CurrencyNotSupportedException.class)
    public ResponseEntity<Object> handleCurrencyNotSupportedException(
            CurrencyNotSupportedException ex) {
        String body = ex.getMessage();
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}
