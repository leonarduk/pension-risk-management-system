package com.leonarduk.finance.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;

@Named
@Path("/stock")
public class StockFeedEndpoint {

	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/ticker/{ticker}/")
	public String displayHistory(@PathParam("ticker") final String ticker, @QueryParam("years") final int years,
			@QueryParam("interpolate") final boolean interpolate) throws IOException {

		final Instrument instrument = Instrument.fromString(ticker);
		final StringBuilder sbBody = new StringBuilder();
		final List<List<DataField>> records = Lists.newArrayList();

		final List<HistoricalQuote> historyData = this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate);

		for (final HistoricalQuote historicalQuote : historyData) {
			final ArrayList<DataField> record = Lists.newArrayList();
			records.add(record);
			record.add(new DataField("Date", historicalQuote.getDate()));
			record.add(new DataField("Open", historicalQuote.getOpen()));
			record.add(new DataField("High", historicalQuote.getHigh()));
			record.add(new DataField("Low", historicalQuote.getLow()));
			record.add(new DataField("Close", historicalQuote.getClose()));
			record.add(new DataField("Volume", historicalQuote.getVolume()));
			record.add(new DataField("Comment", historicalQuote.getComment()));
		}

		HtmlTools.printTable(sbBody, records);
		return HtmlTools.createHtmlText(null, sbBody).toString();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/download/ticker/{ticker}/")
	public Response downloadHistoryCsv(@PathParam("ticker") final String ticker, @QueryParam("years") final int years,
			@QueryParam("interpolate") final boolean interpolate) throws IOException {
		final Instrument instrument = Instrument.fromString(ticker);
		final List<HistoricalQuote> series = this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate);
		final String fileName = instrument.getExchange().name() + "_" + instrument.code() + ".csv";
		final String myCsvText = StockFeed.seriesToCsv(series).toString();
		return Response.ok(myCsvText).header("Content-Disposition", "attachment; filename=" + fileName).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/api/ticker/{ticker}/")
	public List<HistoricalQuote> getHistory(@PathParam("ticker") final String ticker,
			@QueryParam("years") final int years, @QueryParam("interpolate") final boolean interpolate)
			throws IOException {
		final Instrument instrument = Instrument.fromString(ticker);
		return this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate);
	}

	private List<HistoricalQuote> getHistoryData(final Instrument instrument, final int years,
			final boolean interpolate) throws IOException {
		final Optional<Stock> stock = new IntelligentStockFeed().get(instrument, years, interpolate);
		if (stock.isPresent()) {
			return stock.get().getHistory();
		}
		return Lists.newArrayList();
	}

}
