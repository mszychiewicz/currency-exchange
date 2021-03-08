package io.github.mszychiewicz.currencyexchange.domain.command;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class OpenAccountCommand {
    String firstName;
    String secondName;
    BigDecimal openingBalance;
}
