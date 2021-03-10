package io.github.mszychiewicz.currencyexchange.infrastructure;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Currency;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "clients.nbp.baseUrl=http://localhost:${wiremock.server.port}")
class NbpExchangeRateProviderTest {

    @Autowired
    private NpbExchangeRateProvider exchangeRateProvider;

    @Test
    void givenCurrencyAndOkResponse_whenGetAskExchangeRate_thenCorrectAskIsReturned() {
        //given
        Currency USD = Currency.getInstance("USD");
        BigDecimal returnedAskExchangeRate = new BigDecimal("3.4567");
        stubExchangeRateResponse(USD.getCurrencyCode(), "3.8340", returnedAskExchangeRate.toString());
        //when
        BigDecimal askExchangeRate = exchangeRateProvider.getAskExchangeRate(USD);
        //then
        assertEquals(returnedAskExchangeRate, askExchangeRate);
    }

    @Test
    void givenCurrencyAndNotFoundResponse_whenGetAskExchangeRate_thenThrowServiceUnavailableStatus() {
        //given
        Currency USD = Currency.getInstance("USD");
        stubExchangeRateNotFoundResponse(USD.getCurrencyCode());
        //when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exchangeRateProvider.getAskExchangeRate(USD)
        );
        //then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    @Test
    void givenCurrencyAndOkResponse_whenGetBidExchangeRate_thenCorrectAskIsReturned() {
        //given
        Currency USD = Currency.getInstance("USD");
        BigDecimal returnedBidExchangeRate = new BigDecimal("3.4567");
        stubExchangeRateResponse(USD.getCurrencyCode(), "3.8340", returnedBidExchangeRate.toString());
        //when
        BigDecimal askExchangeRate = exchangeRateProvider.getAskExchangeRate(USD);
        //then
        assertEquals(returnedBidExchangeRate, askExchangeRate);
    }

    @Test
    void givenCurrencyAndNotFoundResponse_whenGetBidExchangeRate_thenThrowServiceUnavailableStatus() {
        //given
        Currency USD = Currency.getInstance("USD");
        stubExchangeRateNotFoundResponse(USD.getCurrencyCode());
        //when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exchangeRateProvider.getBidExchangeRate(USD)
        );
        //then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    void stubExchangeRateResponse(String currencyCode, String bidRate, String askRate) {
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

    void stubExchangeRateNotFoundResponse(String currencyCode) {
        stubFor(WireMock.get(urlEqualTo("/api/exchangerates/rates/c/" + currencyCode))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                ));
    }
}