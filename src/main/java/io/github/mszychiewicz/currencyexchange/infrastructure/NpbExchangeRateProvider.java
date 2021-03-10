package io.github.mszychiewicz.currencyexchange.infrastructure;

import io.github.mszychiewicz.currencyexchange.domain.ExchangeRateProvider;
import io.github.mszychiewicz.currencyexchange.infrastructure.response.CurrencyExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NpbExchangeRateProvider implements ExchangeRateProvider {
    private final RestTemplate restTemplate;

    @Value("${clients.nbp.baseUrl}")
    private String baseUrl;

    @Value("${clients.nbp.exchangeRateUrl}")
    private String exchangeRateUrl;

    public Optional<BigDecimal> findAskExchangeRate(Currency currency) {
        try {
            BigDecimal ask = fetchExchangeRate(currency).getRates().get(0).getAsk();
            return Optional.of(ask);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }
    }

    public Optional<BigDecimal> findBidExchangeRate(Currency currency) {
        try {
            BigDecimal bid = fetchExchangeRate(currency).getRates().get(0).getBid();
            return Optional.of(bid);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }
    }

    private CurrencyExchangeRateResponse fetchExchangeRate(Currency currency) {
        ResponseEntity<CurrencyExchangeRateResponse> response =
                restTemplate.getForEntity(
                        baseUrl + exchangeRateUrl + currency.getCurrencyCode(),
                        CurrencyExchangeRateResponse.class
                );
        return response.getBody();
    }
}