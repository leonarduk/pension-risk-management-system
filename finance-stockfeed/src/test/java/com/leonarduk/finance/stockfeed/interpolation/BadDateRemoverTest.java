package com.leonarduk.finance.stockfeed.interpolation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;

import yahoofinance.histquotes.HistoricalQuote;

public class BadDateRemoverTest {

	private BadDateRemover remover;

	@Before
	public void setUp() throws Exception {
		this.remover = new BadDateRemover();
	}

	@Test
	public final void testClean() throws IOException {
		final List<HistoricalQuote> series = IntelligentStockFeed.getFlatCashSeries(Instrument.CASH, 1).get()
				.getHistory();
		final int size = series.size();

		series.add(new HistoricalQuote(series.get(0), LocalDate.parse("2023232-01-01"), "bad point"));
		series.add(new HistoricalQuote(series.get(0), LocalDate.parse("01-01-01"), "bad point"));
		assertEquals(size + 2, series.size());

		assertEquals(size, this.remover.clean(series).size());
	}

}
