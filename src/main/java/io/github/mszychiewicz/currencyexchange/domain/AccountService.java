package io.github.mszychiewicz.currencyexchange.domain;

import io.github.mszychiewicz.currencyexchange.domain.command.BuyCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.OpenAccountCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.SellCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.exception.AccountNotFoundException;
import io.github.mszychiewicz.currencyexchange.domain.exception.CurrencyNotSupportedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static io.github.mszychiewicz.currencyexchange.domain.Account.PLN;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ExchangeRateProvider exchangeRateProvider;

    public static final String CURRENCY_NOT_SUPPORTED_MESSAGE = "Currency not supported.";
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found.";

    public UUID openAccount(OpenAccountCommand openAccountCommand) {
        Account newAccount = new Account(
                openAccountCommand.getFirstName(),
                openAccountCommand.getSecondName(),
                openAccountCommand.getOpeningBalance()
        );
        return accountRepository.save(newAccount);
    }

    public Account getById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    public void buyCurrency(BuyCurrencyCommand buyCurrencyCommand) {
        Account account = getById(buyCurrencyCommand.getId());
        BigDecimal exchangeRate = exchangeRateProvider.findAskExchangeRate(buyCurrencyCommand.getCurrency())
                .orElseThrow(() -> new CurrencyNotSupportedException(CURRENCY_NOT_SUPPORTED_MESSAGE));
        BigDecimal costAmount = buyCurrencyCommand.getAmount().multiply(exchangeRate);

        account.withdrawFunds(PLN, costAmount);
        account.depositFunds(buyCurrencyCommand.getCurrency(), buyCurrencyCommand.getAmount());
        accountRepository.save(account);
    }

    public void sellCurrency(SellCurrencyCommand sellCurrencyCommand) {
        Account account = getById(sellCurrencyCommand.getId());
        account.validateHasSufficientFunds(sellCurrencyCommand.getCurrency(), sellCurrencyCommand.getAmount());
        BigDecimal exchangeRate = exchangeRateProvider.findBidExchangeRate(sellCurrencyCommand.getCurrency())
                .orElseThrow(() -> new CurrencyNotSupportedException(CURRENCY_NOT_SUPPORTED_MESSAGE));
        BigDecimal exchangedAmount = sellCurrencyCommand.getAmount().multiply(exchangeRate);

        account.withdrawFunds(sellCurrencyCommand.getCurrency(), sellCurrencyCommand.getAmount());
        account.depositFunds(PLN, exchangedAmount);
        accountRepository.save(account);
    }
}