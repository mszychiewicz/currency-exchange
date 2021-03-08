package io.github.mszychiewicz.currencyexchange.domain.command;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class BuyCurrencyCommand extends CurrencyCommand {
    public BuyCurrencyCommand(UUID id, Currency currency, BigDecimal amount) {
        super(id, currency, amount);
    }
}
