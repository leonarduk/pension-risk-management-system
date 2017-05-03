package com.leonarduk.finance.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

@Named
@Path("/stock")
public class StockFeedEndpoint {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("history/csv")
	public Response downloadHistoryCsv(@QueryParam("exchange") final String exchange,
			@QueryParam("ticker") final String ticker, @QueryParam("years") final int years) throws IOException {
		final List<HistoricalQuote> series = this.getHistory(exchange, ticker, years);
		final String fileName = exchange + "_" + ticker + ".csv";
		final String myCsvText = StockFeed.seriesToCsv(series).toString();
		return Response.ok(myCsvText).header("Content-Disposition", "attachment; filename=" + fileName).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("history")
	public List<HistoricalQuote> getHistory(@QueryParam("exchange") final String exchange,
			@QueryParam("ticker") final String ticker, @QueryParam("years") final int years) throws IOException {
		return this.getHistoryData(exchange, ticker, years);
	}

	private List<HistoricalQuote> getHistoryData(final String exchange, final String ticker, final int years)
			throws IOException {
		final Optional<Stock> stock = new IntelligentStockFeed().get(Instrument.fromString(ticker), years);
		if (stock.isPresent()) {
			return stock.get().getHistory();
		}
		return Lists.newArrayList();
	}

}
