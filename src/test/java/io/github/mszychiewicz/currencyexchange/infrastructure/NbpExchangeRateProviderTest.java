package io.github.mszychiewicz.currencyexchange.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NbpExchangeRateProviderTest {

    @Autowired
    private NpbExchangeRateProvider exchangeRateProvider;

    @Test
    void shouldFetchAsk() {
        //given
        Currency USD = Currency.getInstance("USD");
        //when
        Optional<BigDecimal> askOptional = exchangeRateProvider.findAskExchangeRate(USD);
        //then
        assertThat(askOptional).isNotEmpty();
    }

    @Test
    void shouldFetchBid() {
        //given
        Currency USD = Currency.getInstance("USD");
        //when
        Optional<BigDecimal> bidOptional = exchangeRateProvider.findBidExchangeRate(USD);
        //then
        assertThat(bidOptional).isNotEmpty();
    }
}