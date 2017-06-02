package com.leonarduk.finance.api;

import java.io.IOException;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;

public class StockFeedEndpointTest {

	private StockFeedEndpoint	point;
	private StockFeed			stockFeed;
	private String				ticker;

	@Before
	public void setUp() throws Exception {
		this.stockFeed = Mockito.mock(StockFeed.class);
		this.point = new StockFeedEndpoint(this.stockFeed);
		this.ticker = "Cash";
	}

	@Test
	public final void testDisplayHistory() throws IOException {
		final boolean interpolate = true;
		final Instrument instrument = Instrument.CASH;
		final Optional<Stock> stock = Optional
		        .of(AbstractStockFeed.createStock(instrument));
		stock.get().setHistory(Lists.newArrayList());
		Mockito.when(
		        this.stockFeed.get(instrument, LocalDate.parse("2017-01-01"),
		                LocalDate.parse("2017-06-01"), interpolate))
		        .thenReturn(stock);
		this.point.displayHistory(this.ticker, 0, "2017-01-01", "2017-06-01",
		        true);
	}

	@Test
	public final void testDownloadHistoryCsv() throws IOException {
		final boolean interpolate = true;
		final Instrument instrument = Instrument.CASH;
		final Optional<Stock> stock = Optional
		        .of(AbstractStockFeed.createStock(instrument));
		Mockito.when(this.stockFeed.get(instrument, 1, interpolate))
		        .thenReturn(stock);
		this.point.downloadHistoryCsv(this.ticker, 1, interpolate);
	}

	@Test
	public final void testDownloadHistoryCsvWithHistory() throws IOException {
		final boolean interpolate = true;
		final Instrument instrument = Instrument.CASH;
		final Optional<Stock> stock = Optional
		        .of(AbstractStockFeed.createStock(instrument));
		stock.get().setHistory(Lists.newArrayList());
		Mockito.when(this.stockFeed.get(instrument, 1, interpolate))
		        .thenReturn(stock);
		this.point.downloadHistoryCsv(this.ticker, 1, interpolate);
	}

	@Test
	public final void testGetHistory() throws IOException {
		final boolean interpolate = true;
		final Instrument instrument = Instrument.CASH;
		final Optional<Stock> stock = Optional
		        .of(AbstractStockFeed.createStock(instrument));
		stock.get().setHistory(Lists.newArrayList());
		Mockito.when(this.stockFeed.get(instrument, 1, interpolate))
		        .thenReturn(stock);
		this.point.getHistory(this.ticker, 1, true);
	}

}
