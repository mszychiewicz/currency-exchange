package io.github.mszychiewicz.currencyexchange.infrastructure;

import io.github.mszychiewicz.currencyexchange.domain.ExchangeRateProvider;
import io.github.mszychiewicz.currencyexchange.infrastructure.response.CurrencyExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Currency;

@Component
@RequiredArgsConstructor
public class NpbExchangeRateProvider implements ExchangeRateProvider {
    private final RestTemplate restTemplate;

    @Value("${clients.nbp.baseUrl}")
    private String baseUrl;

    @Value("${clients.nbp.exchangeRatesPath}")
    private String exchangeRatesUrl;

    public BigDecimal getAskExchangeRate(Currency currency) {
        try {
            return fetchExchangeRates(currency).getRates().get(0).getAsk();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public BigDecimal getBidExchangeRate(Currency currency) {
        try {
            return fetchExchangeRates(currency).getRates().get(0).getBid();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private CurrencyExchangeRateResponse fetchExchangeRates(Currency currency) {
        ResponseEntity<CurrencyExchangeRateResponse> response =
                restTemplate.getForEntity(
                        baseUrl + exchangeRatesUrl + currency.getCurrencyCode(),
                        CurrencyExchangeRateResponse.class
                );
        return response.getBody();
    }
}