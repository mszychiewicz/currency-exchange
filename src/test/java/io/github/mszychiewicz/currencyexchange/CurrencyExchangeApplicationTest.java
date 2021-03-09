package io.github.mszychiewicz.currencyexchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.mszychiewicz.currencyexchange.config.NbpMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyExchangeApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RegisterExtension
    NbpMockServer nbpMockServer = new NbpMockServer();

    @Test
    void givenUserData_whenUserOpensAccount_thenAccountIsCorrect() throws Exception {
        //given user wants to open an account
        String firstName = "Jane";
        String lastName = "Doe";
        BigDecimal plnBalance = BigDecimal.TEN;

        //when they try to open account expect success
        String accountId = openAccount(firstName, lastName, plnBalance);

        //and when user gets their account details expect success
        JsonNode accountDetails = getAccountDetails(accountId);

        //then user account details are correct
        assertEquals(accountId, accountDetails.get("id").textValue());
        assertEquals(firstName, accountDetails.get("firstName").textValue());
        assertEquals(lastName, accountDetails.get("lastName").textValue());
        //and account balance is correct
        BigDecimal plnAccountBalance = new BigDecimal(accountDetails.path("balances").get("PLN").toString());
        assertEquals(plnBalance, plnAccountBalance);
    }

    @Test
    void givenUserAccountWithSufficientBalance_whenUserBuysCurrency_thenExchangeIsCorrect() throws Exception {
        //given user has open account with sufficient balance
        String firstName = "Jane";
        String lastName = "Doe";
        BigDecimal plnBalance = BigDecimal.TEN;
        String accountId = openAccount(firstName, lastName, plnBalance);

        //and wants to buy an amount of USD currency
        String currencyCode = "USD";
        BigDecimal amountToBuy = BigDecimal.ONE;

        //and there are exchange rates
        BigDecimal askExchangeRate = new BigDecimal("3.9112");
        nbpMockServer.stubExchangeRateResponse(currencyCode, "3.8421", askExchangeRate.toString());

        //when they try to buy currency expect success
        buyCurrency(accountId, currencyCode, amountToBuy);

        //and when they try to get their account details expect success
        JsonNode accountDetails = getAccountDetails(accountId);

        //then currency balance is equal to bought amount
        BigDecimal currencyAccountBalance = new BigDecimal(accountDetails.path("balances").get(currencyCode).toString());
        assertEquals(amountToBuy, currencyAccountBalance);

        //and PLN balance is reduced by correct exchanged amount
        BigDecimal plnAccountBalance = new BigDecimal(accountDetails.path("balances").get("PLN").toString());
        assertEquals(plnBalance.subtract(amountToBuy.multiply(askExchangeRate)), plnAccountBalance);
    }

    @Test
    void givenUserAccountWithSufficientBalance_whenUserSellsCurrency_thenExchangeIsCorrect() throws Exception {
        //given user has open account
        String firstName = "Jane";
        String lastName = "Doe";
        BigDecimal plnBalance = BigDecimal.TEN;
        String accountId = openAccount(firstName, lastName, plnBalance);

        //and wants to sell an amount of USD currency
        String currencyCode = "USD";
        BigDecimal amountToSell = BigDecimal.ONE;

        //and there are exchange rates
        BigDecimal bidExchangeRate = new BigDecimal("3.8421");
        BigDecimal askExchangeRate = new BigDecimal("3.9112");
        nbpMockServer.stubExchangeRateResponse(currencyCode, bidExchangeRate.toString(), askExchangeRate.toString());

        //and has sufficient balance
        BigDecimal currencyBalance = BigDecimal.ONE;
        buyCurrency(accountId, currencyCode, currencyBalance);
        plnBalance = plnBalance.subtract(currencyBalance.multiply(askExchangeRate));

        //when they try to sell currency expect success
        sellCurrency(accountId, currencyCode, amountToSell);

        //and when they try to get their account details expect success
        JsonNode accountDetails = getAccountDetails(accountId);

        //then currency balance is reduced by correct amount
        BigDecimal currencyAccountBalance = new BigDecimal(accountDetails.path("balances").get(currencyCode).toString());
        assertEquals(currencyBalance.subtract(amountToSell), currencyAccountBalance);

        //and PLN balance is increased by correct exchanged amount
        BigDecimal plnAccountBalance = new BigDecimal(accountDetails.path("balances").get("PLN").toString());
        assertEquals(plnBalance.add(amountToSell.multiply(bidExchangeRate)), plnAccountBalance);
    }

    private String openAccount(String firstName, String lastName, BigDecimal plnBalance) throws Exception {
        ObjectNode openAccountData = objectMapper.createObjectNode();
        openAccountData.put("firstName", firstName);
        openAccountData.put("lastName", lastName);
        openAccountData.put("openingBalance", plnBalance.toString());

        MvcResult openAccountResult = mvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openAccountData)))
                .andExpect(status().isOk())
                .andReturn();

        String openAccountContent = openAccountResult.getResponse().getContentAsString();
        return objectMapper.readTree(openAccountContent).get("id").textValue();
    }

    private JsonNode getAccountDetails(String accountId) throws Exception {
        MvcResult getAccountDetailsResult = mvc.perform(get("/account/" + accountId))
                .andExpect(status().isOk())
                .andReturn();

        String getAccountDetailsContent = getAccountDetailsResult.getResponse().getContentAsString();
        return objectMapper.readTree(getAccountDetailsContent);
    }

    private void buyCurrency(String accountId, String currencyCode, BigDecimal amount) throws Exception {
        ObjectNode buyCurrencyData = objectMapper.createObjectNode();
        buyCurrencyData.put("currencyCode", currencyCode);
        buyCurrencyData.put("amount", amount.toString());

        mvc.perform(post("/account/" + accountId + "/buy-currency-command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyCurrencyData)))
                .andExpect(status().isOk());
    }

    private void sellCurrency(String accountId, String currencyCode, BigDecimal amount) throws Exception {
        ObjectNode sellCurrencyData = objectMapper.createObjectNode();
        sellCurrencyData.put("currencyCode", currencyCode);
        sellCurrencyData.put("amount", amount.toString());

        mvc.perform(post("/account/" + accountId + "/sell-currency-command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sellCurrencyData)))
                .andExpect(status().isOk());
    }
}
