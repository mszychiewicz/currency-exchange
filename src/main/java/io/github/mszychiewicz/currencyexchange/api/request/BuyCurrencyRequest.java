package io.github.mszychiewicz.currencyexchange.api.request;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Value
public class BuyCurrencyRequest {
    @NotBlank
    String currencyCode;
    @Positive
    BigDecimal amount;
}
