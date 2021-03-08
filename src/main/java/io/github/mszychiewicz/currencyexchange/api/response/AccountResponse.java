package io.github.mszychiewicz.currencyexchange.api.response;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

@Value
public class AccountResponse {
    UUID id;
    String firstName;
    String lastName;
    Map<Currency, BigDecimal> balances;
}
