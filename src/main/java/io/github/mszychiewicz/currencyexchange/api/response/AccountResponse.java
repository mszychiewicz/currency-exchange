package io.github.mszychiewicz.currencyexchange.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    UUID id;
    String firstName;
    String lastName;
    Map<Currency, BigDecimal> balances;
}
