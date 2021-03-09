package io.github.mszychiewicz.currencyexchange.api;

import io.github.mszychiewicz.currencyexchange.api.request.BuyCurrencyRequest;
import io.github.mszychiewicz.currencyexchange.api.request.OpenAccountRequest;
import io.github.mszychiewicz.currencyexchange.api.request.SellCurrencyRequest;
import io.github.mszychiewicz.currencyexchange.api.response.AccountResponse;
import io.github.mszychiewicz.currencyexchange.api.response.OpenAccountResponse;
import io.github.mszychiewicz.currencyexchange.domain.Account;
import io.github.mszychiewicz.currencyexchange.domain.command.BuyCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.OpenAccountCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.SellCurrencyCommand;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Currency;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class AccountApiMapper {
    public OpenAccountCommand toCommand(OpenAccountRequest request) {
        return new OpenAccountCommand(
                request.getFirstName(),
                request.getLastName(),
                request.getOpeningBalance()
        );
    }

    public BuyCurrencyCommand toCommand(UUID id, BuyCurrencyRequest request) {
        validateCurrencyCode(request.getCurrencyCode());
        return new BuyCurrencyCommand(
                id,
                Currency.getInstance(request.getCurrencyCode()),
                request.getAmount()
        );
    }

    public SellCurrencyCommand toCommand(UUID id, SellCurrencyRequest request) {
        validateCurrencyCode(request.getCurrencyCode());
        return new SellCurrencyCommand(
                id,
                Currency.getInstance(request.getCurrencyCode()),
                request.getAmount()
        );
    }

    private void validateCurrencyCode(String code) {
        Set<String> currencyCodes = Currency.getAvailableCurrencies()
                .stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());
        if (!currencyCodes.contains(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getBalances()
        );
    }

    public OpenAccountResponse toResponse(UUID accountId) {
        return new OpenAccountResponse(accountId);
    }
}
