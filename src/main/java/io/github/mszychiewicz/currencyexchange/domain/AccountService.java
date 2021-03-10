package io.github.mszychiewicz.currencyexchange.domain;

import io.github.mszychiewicz.currencyexchange.domain.command.BuyCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.OpenAccountCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.SellCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static io.github.mszychiewicz.currencyexchange.domain.Account.PLN;
import static io.github.mszychiewicz.currencyexchange.domain.SupportedCurrencies.validateCurrencySupport;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ExchangeRateProvider exchangeRateProvider;

    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found.";

    public UUID openAccount(OpenAccountCommand openAccountCommand) {
        Account newAccount = new Account(
                openAccountCommand.getFirstName(),
                openAccountCommand.getLastName(),
                openAccountCommand.getOpeningBalance()
        );
        return accountRepository.save(newAccount);
    }

    public Account getById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    public void buyCurrency(BuyCurrencyCommand buyCurrencyCommand) {
        validateCurrencySupport(buyCurrencyCommand.getCurrency());

        Account account = getById(buyCurrencyCommand.getId());

        BigDecimal exchangeRate = exchangeRateProvider.getAskExchangeRate(buyCurrencyCommand.getCurrency());
        BigDecimal costAmount = buyCurrencyCommand.getAmount().multiply(exchangeRate);

        account.withdrawFunds(PLN, costAmount);
        account.depositFunds(buyCurrencyCommand.getCurrency(), buyCurrencyCommand.getAmount());
        accountRepository.save(account);
    }

    public void sellCurrency(SellCurrencyCommand sellCurrencyCommand) {
        validateCurrencySupport(sellCurrencyCommand.getCurrency());

        Account account = getById(sellCurrencyCommand.getId());
        account.validateHasSufficientFunds(sellCurrencyCommand.getCurrency(), sellCurrencyCommand.getAmount());

        BigDecimal exchangeRate = exchangeRateProvider.getBidExchangeRate(sellCurrencyCommand.getCurrency());
        BigDecimal exchangedAmount = sellCurrencyCommand.getAmount().multiply(exchangeRate);

        account.withdrawFunds(sellCurrencyCommand.getCurrency(), sellCurrencyCommand.getAmount());
        account.depositFunds(PLN, exchangedAmount);
        accountRepository.save(account);
    }
}