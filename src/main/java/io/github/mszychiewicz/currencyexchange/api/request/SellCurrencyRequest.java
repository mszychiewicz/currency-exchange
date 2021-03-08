package io.github.mszychiewicz.currencyexchange.api.request;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Value
public class SellCurrencyRequest {
    @NotBlank
    String currencyCode;
    @Positive
    BigDecimal amount;
}
