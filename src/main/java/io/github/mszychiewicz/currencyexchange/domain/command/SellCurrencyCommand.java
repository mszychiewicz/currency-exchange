package io.github.mszychiewicz.currencyexchange.domain.command;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class SellCurrencyCommand extends CurrencyCommand {
    public SellCurrencyCommand(UUID id, Currency currency, BigDecimal amount) {
        super(id, currency, amount);
    }
}
