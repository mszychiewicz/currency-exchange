package io.github.mszychiewicz.currencyexchange.domain;

import io.github.mszychiewicz.currencyexchange.domain.command.BuyCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.OpenAccountCommand;
import io.github.mszychiewicz.currencyexchange.domain.command.SellCurrencyCommand;
import io.github.mszychiewicz.currencyexchange.domain.exception.AccountNotFoundException;
import io.github.mszychiewicz.currencyexchange.domain.exception.CurrencyNotSupportedException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InsufficientFundsException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InvalidAmountException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InvalidNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AccountServiceUnitTest {

    AccountService accountService;
    AccountRepository accountRepository;
    ExchangeRateProvider exchangeRateProvider;
    @Captor
    private ArgumentCaptor<Account> arg;

    private static final Currency PLN = Currency.getInstance("PLN");
    private static final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        exchangeRateProvider = mock(ExchangeRateProvider.class);
        accountService = new AccountService(accountRepository, exchangeRateProvider);
    }

    @Test
    void givenCommand_whenOpenAccount_thenSaveAccountAndReturnId() {
        //given
        OpenAccountCommand command = new OpenAccountCommand("Jane", "Doe", BigDecimal.TEN);

        when(accountRepository.save(arg.capture())).thenAnswer((Answer<UUID>) invocation -> {
            Object[] args = invocation.getArguments();
            return ((Account) args[0]).getId();
        });

        //when
        UUID accountId = accountService.openAccount(command);

        //then
        verify(accountRepository).save(arg.capture());
        Account savedAccount = arg.getValue();
        assertEquals(accountId, savedAccount.getId());
        assertEquals(command.getFirstName(), savedAccount.getFirstName());
        assertEquals(command.getLastName(), savedAccount.getLastName());
        assertEquals(
                command.getOpeningBalance(),
                savedAccount.getBalances().get(PLN)
        );
    }

    @Test
    void givenBlankFirstName_whenOpenAccount_thenThrowInvalidNameException() {
        //given
        OpenAccountCommand command = new OpenAccountCommand("", "Doe", BigDecimal.TEN);

        //when then
        assertThrows(InvalidNameException.class, () -> accountService.openAccount(command));
    }

    @Test
    void givenBlankLastName_whenOpenAccount_thenThrowInvalidNameException() {
        //given
        OpenAccountCommand command = new OpenAccountCommand("Jane", "", BigDecimal.TEN);

        //when then
        assertThrows(InvalidNameException.class, () -> accountService.openAccount(command));
    }

    @Test
    void givenNegativeOpeningBalance_whenOpenAccount_thenThrowInvalidAmountException() {
        //given
        OpenAccountCommand command = new OpenAccountCommand("Jane", "Doe", BigDecimal.TEN.negate());

        //when then
        assertThrows(InvalidAmountException.class, () -> accountService.openAccount(command));
    }

    @Test
    void givenExistingAccountId_whenGetById_thenReturnAccount() {
        //given
        Account existingAccount = new Account("Jane", "Doe", BigDecimal.TEN);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));

        //when
        Account account = accountService.getById(existingAccount.getId());

        //then
        assertEquals(existingAccount, account);
    }

    @Test
    void givenNonexistentAccountId_whenGetById_thenThrowAccountNotFoundException() {
        //given
        UUID nonexistentId = UUID.randomUUID();
        when(accountRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        //when then
        assertThrows(AccountNotFoundException.class, () -> accountService.getById(nonexistentId));
    }

    @Test
    void givenCommandAndExchangeRateAndAccountWithSufficientFunds_whenBuyCurrency_thenSaveCorrectBalance() {
        //given
        BigDecimal existingPlnBalance = BigDecimal.TEN;
        Account existingAccount = new Account("Jane", "Doe", existingPlnBalance);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));
        BuyCurrencyCommand command = new BuyCurrencyCommand(existingAccount.getId(), USD, BigDecimal.ONE);
        BigDecimal askExchangeRate = new BigDecimal("3.9123");
        when(exchangeRateProvider.findAskExchangeRate(command.getCurrency())).thenReturn(Optional.of(askExchangeRate));

        when(accountRepository.save(arg.capture())).thenReturn(existingAccount.getId());

        //when
        accountService.buyCurrency(command);

        //then
        verify(accountRepository).save(arg.capture());
        Account savedAccount = arg.getValue();
        assertEquals(
                savedAccount.getBalances().get(PLN),
                existingPlnBalance.subtract(command.getAmount().multiply(askExchangeRate))
        );
        assertEquals(
                savedAccount.getBalances().get(command.getCurrency()),
                command.getAmount()
        );
    }

    @Test
    void givenAccountWithInsufficientFunds_whenBuyCurrency_thenThrowInsufficientFundsException() {
        //given
        Account existingAccount = new Account("Jane", "Doe", BigDecimal.TEN);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));
        BigDecimal askExchangeRate = new BigDecimal("3.9123");
        when(exchangeRateProvider.findAskExchangeRate(USD)).thenReturn(Optional.of(askExchangeRate));
        BuyCurrencyCommand command = new BuyCurrencyCommand(existingAccount.getId(), USD, BigDecimal.TEN);

        //when then
        assertThrows(InsufficientFundsException.class, () -> accountService.buyCurrency(command));
    }

    @Test
    void givenNoAskExchangeRate_whenBuyCurrency_thenThrowCurrencyNotSupportedException() {
        //given
        Account existingAccount = new Account("Jane", "Doe", BigDecimal.TEN);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));
        BuyCurrencyCommand command = new BuyCurrencyCommand(existingAccount.getId(), USD, BigDecimal.ONE);
        when(exchangeRateProvider.findAskExchangeRate(command.getCurrency())).thenReturn(Optional.empty());

        //when then
        assertThrows(CurrencyNotSupportedException.class, () -> accountService.buyCurrency(command));
    }

    @Test
    void givenCommandAndExchangeRateAndAccountWithSufficientFunds_whenSellCurrency_thenSaveCorrectBalance() {
        //given
        BigDecimal existingPlnBalance = BigDecimal.TEN;
        BigDecimal existingCurrencyBalance = BigDecimal.TEN;
        Account existingAccount = new Account("Jane", "Doe", existingPlnBalance);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));
        SellCurrencyCommand command = new SellCurrencyCommand(existingAccount.getId(), USD, BigDecimal.ONE);
        existingAccount.depositFunds(command.getCurrency(), existingCurrencyBalance);
        BigDecimal bidExchangeRate = new BigDecimal("3.9123");
        when(exchangeRateProvider.findBidExchangeRate(command.getCurrency())).thenReturn(Optional.of(bidExchangeRate));

        when(accountRepository.save(arg.capture())).thenReturn(existingAccount.getId());

        //when
        accountService.sellCurrency(command);

        //then
        verify(accountRepository).save(arg.capture());
        Account savedAccount = arg.getValue();
        assertEquals(
                savedAccount.getBalances().get(PLN),
                existingPlnBalance.add(command.getAmount().multiply(bidExchangeRate))
        );
        assertEquals(
                savedAccount.getBalances().get(command.getCurrency()),
                existingCurrencyBalance.subtract(command.getAmount())
        );
    }

    @Test
    void givenAccountWithInsufficientFunds_whenSellCurrency_thenThrowInsufficientFundsException() {
        //given
        Account existingAccount = new Account("Jane", "Doe", BigDecimal.ONE);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));
        SellCurrencyCommand command = new SellCurrencyCommand(existingAccount.getId(), USD, BigDecimal.TEN);

        //when then
        assertThrows(InsufficientFundsException.class, () -> accountService.sellCurrency(command));
    }

    @Test
    void givenNoBidExchangeRate_whenSellCurrency_thenThrowCurrencyNotSupportedException() {
        //given
        Account existingAccount = new Account("Jane", "Doe", BigDecimal.TEN);
        SellCurrencyCommand command = new SellCurrencyCommand(existingAccount.getId(), USD, BigDecimal.ONE);
        existingAccount.depositFunds(command.getCurrency(), BigDecimal.TEN);
        when(accountRepository.findById(existingAccount.getId())).thenReturn(Optional.of(existingAccount));
        when(exchangeRateProvider.findAskExchangeRate(command.getCurrency())).thenReturn(Optional.empty());

        //when then
        assertThrows(CurrencyNotSupportedException.class, () -> accountService.sellCurrency(command));
    }
}
