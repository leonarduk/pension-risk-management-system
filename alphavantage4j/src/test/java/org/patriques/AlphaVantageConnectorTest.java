package org.patriques;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.patriques.input.Function;
import org.patriques.input.Symbol;
import org.patriques.output.AlphaVantageException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AlphaVantageConnector} that mock HTTP calls using WireMock.
 */
public class AlphaVantageConnectorTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testGetRequestBuildsUrlAndReturnsResponse() {
        stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("TIME_SERIES_DAILY"))
                .withQueryParam("symbol", equalTo("IBM"))
                .withQueryParam("apikey", equalTo("demo"))
                .willReturn(aResponse().withBody("{\"result\":\"ok\"}")));

        AlphaVantageConnector connector =
                new AlphaVantageConnector("demo", 5000, wireMockServer.baseUrl() + "/query?");

        String response = connector.getRequest(Function.TIME_SERIES_DAILY, new Symbol("IBM"));

        assertEquals("{\"result\":\"ok\"}", response);
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("TIME_SERIES_DAILY"))
                .withQueryParam("symbol", equalTo("IBM"))
                .withQueryParam("apikey", equalTo("demo")));
    }

    @Test
    public void testGetRequestThrowsAlphaVantageExceptionOnIoError() {
        stubFor(get(urlPathEqualTo("/query"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        AlphaVantageConnector connector =
                new AlphaVantageConnector("demo", 5000, wireMockServer.baseUrl() + "/query?");

        assertThrows(AlphaVantageException.class,
                () -> connector.getRequest(Function.TIME_SERIES_DAILY));
    }
}

