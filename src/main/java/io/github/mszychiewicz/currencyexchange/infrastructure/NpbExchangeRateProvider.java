package io.github.mszychiewicz.currencyexchange.infrastructure;

import io.github.mszychiewicz.currencyexchange.domain.ExchangeRateProvider;
import io.github.mszychiewicz.currencyexchange.infrastructure.response.CurrencyExchangeRateResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NpbExchangeRateProvider implements ExchangeRateProvider {
    private final RestTemplate restTemplate;

    private static final String BASE_URL = "http://api.nbp.pl/api/exchangerates/rates/c/";

    public Optional<BigDecimal> findAskExchangeRate(Currency currency) {
        try {
            BigDecimal ask = fetchExchangeRate(currency).getRates().get(0).getAsk();
            return Optional.of(ask);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public Optional<BigDecimal> findBidExchangeRate(Currency currency) {
        try {
            BigDecimal bid = fetchExchangeRate(currency).getRates().get(0).getBid();
            return Optional.of(bid);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private CurrencyExchangeRateResponse fetchExchangeRate(Currency currency) {
        ResponseEntity<CurrencyExchangeRateResponse> response =
                restTemplate.getForEntity(
                        BASE_URL + currency.getCurrencyCode(),
                        CurrencyExchangeRateResponse.class
                );

        return response.getBody();
    }
}