package org.patriques;

import org.junit.jupiter.api.Test;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.ApiParameter;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.*;
import org.patriques.output.technicalindicators.data.IndicatorData;

import static org.junit.jupiter.api.Assertions.*;

public class TechnicalIndicatorsTest {

    private static final String ERROR_JSON = "{\"Error Message\":\"Test error\"}";

    private ApiConnector connectorWith(final String json) {
        return (ApiParameter... params) -> json;
    }

    @Test
    public void testAdSuccess() {
        String json = "{\"Meta Data\":{\"1: Symbol\":\"IBM\"}," +
                "\"Technical Analysis: Chaikin A/D\":{\"2024-01-01\":{\"Chaikin A/D\":\"10.0\"}}}";
        TechnicalIndicators ti = new TechnicalIndicators(connectorWith(json));
        AD ad = ti.ad("IBM", Interval.DAILY);
        IndicatorData data = ad.getIndicatorData().get(0);
        assertEquals(10.0, data.getData(), 0.0);
    }

    @Test
    public void testAdError() {
        TechnicalIndicators ti = new TechnicalIndicators(connectorWith(ERROR_JSON));
        assertThrows(AlphaVantageException.class, () -> ti.ad("IBM", Interval.DAILY));
    }

    @Test
    public void testAdoscWithOptionalParameters() {
        String json = "{\"Meta Data\":{\"1: Symbol\":\"IBM\"}," +
                "\"Technical Analysis: ADOSC\":{\"2024-01-01\":{\"ADOSC\":\"10.0\"}}}";
        TechnicalIndicators ti = new TechnicalIndicators(connectorWith(json));
        ADOSC adosc = ti.adosc("IBM", Interval.DAILY, null, null);
        assertEquals(10.0, adosc.getIndicatorData().get(0).getData(), 0.0);
    }

    @Test
    public void testApoWithOptionalParameters() {
        String json = "{\"Meta Data\":{\"1: Symbol\":\"IBM\"}," +
                "\"Technical Analysis: APO\":{\"2024-01-01\":{\"APO\":\"10.0\"}}}";
        TechnicalIndicators ti = new TechnicalIndicators(connectorWith(json));
        APO apo = ti.apo("IBM", Interval.DAILY, SeriesType.CLOSE, null, null, null);
        assertEquals(10.0, apo.getIndicatorData().get(0).getData(), 0.0);
    }
}

