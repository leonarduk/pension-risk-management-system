package org.patriques;

import org.junit.jupiter.api.Test;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.exchange.CurrencyExchange;
import org.patriques.output.exchange.Daily;
import org.patriques.output.exchange.data.CurrencyExchangeData;
import org.patriques.output.exchange.data.ForexData;
import org.patriques.input.ApiParameter;

import static org.junit.jupiter.api.Assertions.*;

public class ForeignExchangeTest {

    private static final String ERROR_JSON = "{\"Error Message\":\"Test error\"}";

    private ApiConnector connectorWith(final String json) {
        return (ApiParameter... params) -> json;
    }

    @Test
    public void testCurrencyExchangeRateSuccess() {
        String json = "{\"Realtime Currency Exchange Rate\":{" +
                "\"1. From_Currency Code\":\"USD\",\"2. From_Currency Name\":\"United States Dollar\"," +
                "\"3. To_Currency Code\":\"JPY\",\"4. To_Currency Name\":\"Japanese Yen\"," +
                "\"5. Exchange Rate\":\"110.00\",\"6. Last Refreshed\":\"2024-01-01 00:00:00\",\"7. Time Zone\":\"UTC\"}}";
        ForeignExchange fx = new ForeignExchange(connectorWith(json));
        CurrencyExchange ce = fx.currencyExchangeRate("USD", "JPY");
        CurrencyExchangeData data = ce.getData();
        assertEquals("USD", data.getFromCurrencyCode());
        assertEquals(110.00f, data.getExchangeRate(), 0.0f);
    }

    @Test
    public void testCurrencyExchangeRateError() {
        ForeignExchange fx = new ForeignExchange(connectorWith(ERROR_JSON));
        assertThrows(AlphaVantageException.class, () -> fx.currencyExchangeRate("USD", "JPY"));
    }

    @Test
    public void testDailySuccess() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Time Series FX (Daily)\":{\"2024-01-01\":{\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\"}}}";
        ForeignExchange fx = new ForeignExchange(connectorWith(json));
        Daily daily = fx.daily("EUR", "USD", OutputSize.COMPACT);
        ForexData data = daily.getForexData().get(0);
        assertEquals(1.0, data.getOpen(), 0.0);
    }

    @Test
    public void testDailyError() {
        ForeignExchange fx = new ForeignExchange(connectorWith(ERROR_JSON));
        assertThrows(AlphaVantageException.class,
                () -> fx.daily("EUR", "USD", OutputSize.COMPACT));
    }
}

