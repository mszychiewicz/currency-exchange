package io.github.mszychiewicz.currencyexchange.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

public interface ExchangeRateProvider {
    Optional<BigDecimal> findAskExchangeRate(Currency currency);

    Optional<BigDecimal> findBidExchangeRate(Currency currency);
}
