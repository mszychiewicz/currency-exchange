package io.github.mszychiewicz.currencyexchange.infrastructure.response;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ExchangeRateResponse {
    String no;
    String effectiveDate;
    BigDecimal ask;
    BigDecimal bid;
}