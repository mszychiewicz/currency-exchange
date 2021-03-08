package io.github.mszychiewicz.currencyexchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mszychiewicz.currencyexchange.api.request.BuyCurrencyRequest;
import io.github.mszychiewicz.currencyexchange.api.request.OpenAccountRequest;
import io.github.mszychiewicz.currencyexchange.api.request.SellCurrencyRequest;
import io.github.mszychiewicz.currencyexchange.api.response.AccountResponse;
import io.github.mszychiewicz.currencyexchange.api.response.OpenAccountResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO mock nbp api using wiremock

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyExchangeApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void openAccountAndGetDetails() throws Exception {
        //given open account request
        OpenAccountRequest openAccountRequest = new OpenAccountRequest("Jane", "Doe", BigDecimal.TEN);

        //when open account
        MvcResult openAccountResult = mvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openAccountRequest)))
                .andExpect(status().isOk())
                .andReturn();

        //then account id is returned
        String openAccountContent = openAccountResult.getResponse().getContentAsString();
        UUID accountId = objectMapper.readValue(openAccountContent, OpenAccountResponse.class).getId();

        //when get account details
        MvcResult getAccountDetailsResult = mvc.perform(get("/account/" + accountId))
                .andExpect(status().isOk())
                .andReturn();

        //then account details match request
        String getAccountDetailsContent = getAccountDetailsResult.getResponse().getContentAsString();
        AccountResponse response = objectMapper.readValue(getAccountDetailsContent, AccountResponse.class);

        assert (response.getFirstName().equals(openAccountRequest.getFirstName()));
        assert (response.getLastName().equals(openAccountRequest.getSecondName()));
        assert (response.getBalances().get(
                Currency.getInstance("PLN")).equals(openAccountRequest.getOpeningBalance())
        );
    }

    @Test
    void openAccountAndBuyUsd() throws Exception {
        //given open account request and buy currency request
        OpenAccountRequest openAccountRequest = new OpenAccountRequest("Jane", "Doe", BigDecimal.TEN);
        BuyCurrencyRequest buyCurrencyRequest = new BuyCurrencyRequest("USD", BigDecimal.ONE);

        //when open account
        MvcResult openAccountResult = mvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openAccountRequest)))
                .andExpect(status().isOk())
                .andReturn();

        //then account id is returned
        String openAccountContent = openAccountResult.getResponse().getContentAsString();
        UUID accountId = objectMapper.readValue(openAccountContent, OpenAccountResponse.class).getId();

        //when buy usd
        mvc.perform(post("/account/" + accountId + "/buy-currency-command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyCurrencyRequest)))
                .andExpect(status().isOk());

        //and when get account details
        MvcResult getAccountDetailsResult = mvc.perform(get("/account/" + accountId))
                .andExpect(status().isOk())
                .andReturn();

        //then account pln balance is lower than opening balance and currency balance matches buy request
        String getAccountDetailsContent = getAccountDetailsResult.getResponse().getContentAsString();
        AccountResponse response = objectMapper.readValue(getAccountDetailsContent, AccountResponse.class);

        assert (response.getFirstName().equals(openAccountRequest.getFirstName()));
        assert (response.getLastName().equals(openAccountRequest.getSecondName()));
        assert (response.getBalances().get(
                Currency.getInstance("PLN")).compareTo(openAccountRequest.getOpeningBalance()) < 0
        );
        assert (response.getBalances().get(
                Currency.getInstance(buyCurrencyRequest.getCurrencyCode())).equals(buyCurrencyRequest.getAmount())
        );
    }

    @Test
    void openAccountAndBuyThenSellUsd() throws Exception {
        //given open account request and buy currency request
        OpenAccountRequest openAccountRequest = new OpenAccountRequest("Jane", "Doe", BigDecimal.TEN);
        BuyCurrencyRequest buyCurrencyRequest = new BuyCurrencyRequest("USD", BigDecimal.ONE);
        SellCurrencyRequest sellCurrencyRequest = new SellCurrencyRequest("USD", BigDecimal.ONE);

        //when open account
        MvcResult openAccountResult = mvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openAccountRequest)))
                .andExpect(status().isOk())
                .andReturn();

        //then account id is returned
        String openAccountContent = openAccountResult.getResponse().getContentAsString();
        UUID accountId = objectMapper.readValue(openAccountContent, OpenAccountResponse.class).getId();

        //when buy usd
        mvc.perform(post("/account/" + accountId + "/buy-currency-command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyCurrencyRequest)))
                .andExpect(status().isOk());

        //and when next sell usd
        mvc.perform(post("/account/" + accountId + "/sell-currency-command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sellCurrencyRequest)))
                .andExpect(status().isOk());

        //and when get account details
        MvcResult getAccountDetailsResult = mvc.perform(get("/account/" + accountId))
                .andExpect(status().isOk())
                .andReturn();

        //then account pln balance is lower than opening balance due to ask and bid spread
        //and usd balance is 0
        String getAccountDetailsContent = getAccountDetailsResult.getResponse().getContentAsString();
        AccountResponse response = objectMapper.readValue(getAccountDetailsContent, AccountResponse.class);

        assert (response.getFirstName().equals(openAccountRequest.getFirstName()));
        assert (response.getLastName().equals(openAccountRequest.getSecondName()));
        assert (response.getBalances().get(
                Currency.getInstance("PLN")).compareTo(openAccountRequest.getOpeningBalance()) < 0
        );
        assert (response.getBalances().get(
                Currency.getInstance(buyCurrencyRequest.getCurrencyCode())).equals(BigDecimal.ZERO)
        );
    }
}
