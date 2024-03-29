package com.leonarduk.finance.stockfeed.interpolation;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.datatransformation.correction.BadDateRemover;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class BadDateRemoverTest {

    private BadDateRemover remover;

    @Before
    public void setUp() throws Exception {
        this.remover = new BadDateRemover();
    }

    @Test
    public final void testClean() throws IOException {
        IntelligentStockFeed feed = new IntelligentStockFeed(new FileBasedDataStore("db"));
        final List<Bar> series = feed
                .getFlatCashSeries(Instrument.CASH, 1).get().getHistory();
        final int size = series.size();

        series.add(new ExtendedHistoricalQuote(series.get(0),
                LocalDate.parse("1232-01-01"), "bad point"));
        series.add(new ExtendedHistoricalQuote(series.get(0),
                LocalDate.parse("1001-01-01"), "bad point"));
        Assert.assertEquals(size + 2, series.size());

        Assert.assertEquals(size, this.remover.clean(series).size());
    }

}
