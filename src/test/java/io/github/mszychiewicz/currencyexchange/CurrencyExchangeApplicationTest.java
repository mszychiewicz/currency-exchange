package io.github.mszychiewicz.currencyexchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "clients.nbp.baseUrl=http://localhost:${wiremock.server.port}")
class CurrencyExchangeApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void givenUserAccountWithSufficientBalance_whenUserBuysUsd_thenExchangeIsCorrect() throws Exception {
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
        stubExchangeRateResponse(currencyCode, "3.8421", askExchangeRate.toString());

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
    void givenUserAccountWithSufficientBalance_whenUserSellsUsd_thenExchangeIsCorrect() throws Exception {
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
        stubExchangeRateResponse(currencyCode, bidExchangeRate.toString(), askExchangeRate.toString());

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

        MvcResult openAccountResult = mvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openAccountData)))
                .andExpect(status().isOk())
                .andReturn();

        String openAccountContent = openAccountResult.getResponse().getContentAsString();
        return objectMapper.readTree(openAccountContent).get("id").textValue();
    }

    private JsonNode getAccountDetails(String accountId) throws Exception {
        MvcResult getAccountDetailsResult = mvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andReturn();

        String getAccountDetailsContent = getAccountDetailsResult.getResponse().getContentAsString();
        return objectMapper.readTree(getAccountDetailsContent);
    }

    private void buyCurrency(String accountId, String currencyCode, BigDecimal amount) throws Exception {
        ObjectNode buyCurrencyData = objectMapper.createObjectNode();
        buyCurrencyData.put("currencyCode", currencyCode);
        buyCurrencyData.put("amount", amount.toString());

        mvc.perform(post("/accounts/" + accountId + "/buy-currency-commands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyCurrencyData)))
                .andExpect(status().isOk());
    }

    private void sellCurrency(String accountId, String currencyCode, BigDecimal amount) throws Exception {
        ObjectNode sellCurrencyData = objectMapper.createObjectNode();
        sellCurrencyData.put("currencyCode", currencyCode);
        sellCurrencyData.put("amount", amount.toString());

        mvc.perform(post("/accounts/" + accountId + "/sell-currency-commands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sellCurrencyData)))
                .andExpect(status().isOk());
    }

    public void stubExchangeRateResponse(String currencyCode, String bidRate, String askRate) {
        String responseBody = "{\n" +
                "  \"table\": \"C\",\n" +
                "  \"currency\": \"dolar amerykański\",\n" +
                "  \"code\": \"" + currencyCode + "\",\n" +
                "  \"rates\": [\n" +
                "    {\n" +
                "      \"no\": \"046/C/NBP/2021\",\n" +
                "      \"effectiveDate\": \"2021-03-09\",\n" +
                "      \"bid\": " + bidRate + ",\n" +
                "      \"ask\": " + askRate + "\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        stubFor(WireMock.get(urlEqualTo("/api/exchangerates/rates/c/" + currencyCode))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)
                ));
    }
}
