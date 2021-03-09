package io.github.mszychiewicz.currencyexchange.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class NbpMockServer extends WireMockServer implements BeforeEachCallback, AfterEachCallback {

    public NbpMockServer() {
        // TODO dynamic port allocation
        super(8089);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        this.start();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        this.stop();
        this.resetAll();
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
