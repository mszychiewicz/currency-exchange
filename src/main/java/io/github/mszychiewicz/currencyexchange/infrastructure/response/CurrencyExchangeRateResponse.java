package io.github.mszychiewicz.currencyexchange.infrastructure.response;

import lombok.Value;

import java.util.List;

@Value
public class CurrencyExchangeRateResponse {
    String table;
    String currency;
    String code;
    List<ExchangeRateResponse> rates;
}