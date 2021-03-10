package io.github.mszychiewicz.currencyexchange.domain;

import java.math.BigDecimal;
import java.util.Currency;

public interface ExchangeRateProvider {
    BigDecimal getAskExchangeRate(Currency currency);

    BigDecimal getBidExchangeRate(Currency currency);
}
