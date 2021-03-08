package io.github.mszychiewicz.currencyexchange.api;

import io.github.mszychiewicz.currencyexchange.api.request.BuyCurrencyRequest;
import io.github.mszychiewicz.currencyexchange.api.request.OpenAccountRequest;
import io.github.mszychiewicz.currencyexchange.api.request.SellCurrencyRequest;
import io.github.mszychiewicz.currencyexchange.api.response.AccountResponse;
import io.github.mszychiewicz.currencyexchange.api.response.OpenAccountResponse;
import io.github.mszychiewicz.currencyexchange.domain.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;
    private final AccountApiMapper accountApiMapper;

    @GetMapping("/{id}")
    public @ResponseBody
    AccountResponse getById(@PathVariable("id") UUID id) {
        return accountApiMapper.toResponse(accountService.getById(id));
    }

    @PostMapping
    public @ResponseBody
    OpenAccountResponse post(@Valid @RequestBody OpenAccountRequest request) {
        UUID accountId = accountService.openAccount(accountApiMapper.toCommand(request));
        return accountApiMapper.toResponse(accountId);
    }

    @PostMapping("/{id}/buy-currency-command")
    @ResponseStatus(HttpStatus.OK)
    public void buyCurrency(@PathVariable("id") UUID id,
                            @Valid @RequestBody BuyCurrencyRequest request) {
        accountService.buyCurrency(accountApiMapper.toCommand(id, request));
    }

    @PostMapping("/{id}/sell-currency-command")
    @ResponseStatus(HttpStatus.OK)
    public void sellCurrency(@PathVariable("id") UUID id,
                             @Valid @RequestBody SellCurrencyRequest request) {
        accountService.sellCurrency(accountApiMapper.toCommand(id, request));
    }
}
