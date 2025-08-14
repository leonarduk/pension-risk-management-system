package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.ta4j.core.Bar;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.*;

public class FTTimeSeriesPageTest {

    private WebDriver webDriver;

    @Before
    public void setUp() {
        webDriver = new HtmlUnitDriver();
    }

    @After
    public void tearDown() {
        webDriver.quit();
    }

    @Test
    public void getTimeseriesFiltersByDateRange() throws Exception {
        URL resource = getClass().getResource("/ft_timeseries_sample.html");
        assertNotNull("Sample data should be available", resource);

        FTTimeSeriesPage page = new FTTimeSeriesPage(webDriver, resource.toString());
        Instrument instrument = Instrument.fromString("PHGP");
        LocalDate from = LocalDate.of(2021, 7, 1);
        LocalDate to = LocalDate.of(2021, 8, 31);

        List<Bar> bars = page.getTimeseries(instrument, from, to);

        assertEquals(2, bars.size());
        for (Bar bar : bars) {
            LocalDate date = bar.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate();
            assertFalse(date.isBefore(from));
            assertFalse(date.isAfter(to));
        }
    }
}
