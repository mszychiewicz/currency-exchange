package io.github.mszychiewicz.currencyexchange.infrastructure;

import io.github.mszychiewicz.currencyexchange.config.NbpMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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

    @RegisterExtension
    NbpMockServer mockNbpServer = new NbpMockServer();

    @Test
    void shouldFetchAsk() {
        //given
        Currency USD = Currency.getInstance("USD");
        String askRate = "3.4567";
        mockNbpServer.stubExchangeRateResponse(USD.getCurrencyCode(), "3.8340", askRate);
        //when
        Optional<BigDecimal> askOptional = exchangeRateProvider.findAskExchangeRate(USD);
        //then
        assertThat(askOptional).isNotEmpty();
        assertThat(askOptional.get()).isEqualByComparingTo(askRate);
    }

    @Test
    void shouldFetchBid() {
        //given
        Currency USD = Currency.getInstance("USD");
        String bidRate = "3.4567";
        mockNbpServer.stubExchangeRateResponse(USD.getCurrencyCode(), bidRate, "3.9114");
        //when
        Optional<BigDecimal> bidOptional = exchangeRateProvider.findBidExchangeRate(USD);
        //then
        assertThat(bidOptional).isNotEmpty();
        assertThat(bidOptional.get()).isEqualByComparingTo(bidRate);
    }
}