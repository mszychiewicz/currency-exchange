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

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "clients.nbp.baseUrl=http://localhost:${wiremock.server.port}")
class NbpExchangeRateProviderTest {

    @Autowired
    private NpbExchangeRateProvider exchangeRateProvider;

    @Test
    void shouldFetchAsk() {
        //given
        Currency USD = Currency.getInstance("USD");
        String askRate = "3.4567";
        stubExchangeRateResponse(USD.getCurrencyCode(), "3.8340", askRate);
        //when
        Optional<BigDecimal> askOptional = exchangeRateProvider.findAskExchangeRate(USD);
        //then
        assertThat(askOptional).isNotEmpty();
        assertThat(askOptional.get()).isEqualByComparingTo(askRate);
    }

    @Test
    void shouldFetchBid() {
        //given
        Currency USD = Currency.getInstance("USD");
        String bidRate = "3.4567";
        stubExchangeRateResponse(USD.getCurrencyCode(), bidRate, "3.9114");
        //when
        Optional<BigDecimal> bidOptional = exchangeRateProvider.findBidExchangeRate(USD);
        //then
        assertThat(bidOptional).isNotEmpty();
        assertThat(bidOptional.get()).isEqualByComparingTo(bidRate);
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