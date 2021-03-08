package io.github.mszychiewicz.currencyexchange.domain;

import io.github.mszychiewicz.currencyexchange.domain.exception.InsufficientFundsException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InvalidAmountException;
import io.github.mszychiewicz.currencyexchange.domain.exception.InvalidNameException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountUnitTest {

    @Test
    void shouldCreateAccount() {
        //given
        String firstName = "Jane";
        String lastName = "Doe";
        BigDecimal openingBalance = BigDecimal.TEN;
        //when
        Account account = new Account(
                firstName,
                lastName,
                openingBalance
        );
        //then
        Currency expectedCurrency = Currency.getInstance("PLN");
        assert (account.getFirstName()).equals(firstName);
        assert (account.getLastName()).equals(lastName);
        assert (account.getBalances().get(expectedCurrency)).equals(openingBalance);
    }

    @Test
    void shouldNotCreateAccountWithBlankFirstName() {
        //given
        String firstName = "";
        String lastName = "Doe";
        BigDecimal openingBalance = BigDecimal.TEN;
        //when
        assertThrows(InvalidNameException.class, () -> new Account(
                firstName,
                lastName,
                openingBalance
        ));
    }

    @Test
    void shouldNotCreateAccountWithBlankLastName() {
        //given
        String firstName = "Jane";
        String lastName = "";
        BigDecimal openingBalance = BigDecimal.TEN;
        //when
        assertThrows(InvalidNameException.class, () -> new Account(
                firstName,
                lastName,
                openingBalance
        ));
    }

    @Test
    void shouldNotCreateAccountWithNegativeOpeningBalance() {
        //given
        String firstName = "Jane";
        String lastName = "Doe";
        BigDecimal negativeOpeningBalance = BigDecimal.TEN.negate();
        //when then
        assertThrows(InvalidAmountException.class, () -> new Account(
                firstName,
                lastName,
                negativeOpeningBalance
        ));
    }

    @Test
    void shouldDepositFunds() {
        //given
        Account account = new Account(
                "Jane",
                "Doe",
                BigDecimal.TEN
        );
        Currency currency = Currency.getInstance("USD");
        BigDecimal amount = BigDecimal.ONE;
        //when
        account.depositFunds(currency, amount);

        //then
        assert (account.getBalances().get(currency).equals(amount));
    }

    @Test
    void shouldNotDepositNegativeFunds() {
        //given
        Account account = new Account(
                "Jane",
                "Doe",
                BigDecimal.TEN
        );
        Currency currency = Currency.getInstance("USD");
        BigDecimal negativeAmount = BigDecimal.ONE.negate();
        //when then
        assertThrows(InvalidAmountException.class, () -> account.depositFunds(currency, negativeAmount));
    }

    @Test
    void shouldWithdrawFunds() {
        //given
        BigDecimal openingBalance = BigDecimal.TEN;
        Account account = new Account(
                "Jane",
                "Doe",
                openingBalance
        );
        Currency currency = Currency.getInstance("PLN");
        BigDecimal amount = BigDecimal.ONE;
        //when
        account.withdrawFunds(currency, amount);
        //then
        assert (account.getBalances().get(currency).equals(openingBalance.subtract(amount)));
    }

    @Test
    void shouldNotWithdrawNegativeAmount() {
        //given
        Account account = new Account(
                "Jane",
                "Doe",
                BigDecimal.TEN
        );
        Currency currency = Currency.getInstance("PLN");
        BigDecimal negativeAmount = BigDecimal.ONE.negate();
        //when then
        assertThrows(InvalidAmountException.class, () -> account.withdrawFunds(currency, negativeAmount));
    }

    @Test
    void shouldNotWithdrawFundsWhenInsufficientFundsOnAccount() {
        //given
        Account account = new Account(
                "Jane",
                "Doe",
                BigDecimal.TEN
        );
        Currency currency = Currency.getInstance("USD");
        BigDecimal amount = BigDecimal.ONE;
        //when then
        assertThrows(InsufficientFundsException.class, () -> account.withdrawFunds(currency, amount));
    }

    @Test
    void shouldValidateAccountHasSufficientFunds_pos() {
        //given
        Account account = new Account(
                "Jane",
                "Doe",
                BigDecimal.TEN
        );
        Currency currency = Currency.getInstance("PLN");
        BigDecimal amount = BigDecimal.ONE;
        //when
        account.validateHasSufficientFunds(currency, amount);
        //then
        assertDoesNotThrow(() -> account.withdrawFunds(currency, amount));
    }

    @Test
    void shouldValidateAccountHasSufficientFunds_neg() {
        //given
        BigDecimal openingBalance = BigDecimal.TEN;
        Account account = new Account(
                "Jane",
                "Doe",
                openingBalance
        );
        Currency currency = Currency.getInstance("PLN");
        BigDecimal amount = openingBalance.add(BigDecimal.ONE);
        //when then
        assertThrows(InsufficientFundsException.class, () -> account.withdrawFunds(currency, amount));
    }
}
