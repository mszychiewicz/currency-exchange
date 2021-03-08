package io.github.mszychiewicz.currencyexchange.domain.command;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Getter
@AllArgsConstructor
class CurrencyCommand {
    private final UUID id;
    private final Currency currency;
    private final BigDecimal amount;
}
