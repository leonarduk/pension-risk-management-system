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

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.StockFeed.EXCHANGE;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

@Named
@Path("/stock")
public class StockFeedEndpoint {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("history")
	public List<HistoricalQuote> getHistory(@QueryParam("ticker") String ticker, @QueryParam("years") int years)
			throws IOException {
		return getHistoryData(ticker, years);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("history/csv")
	public Response downloadHistoryCsv(@QueryParam("ticker") String ticker, @QueryParam("years") int years) throws IOException {
		List<HistoricalQuote> series = getHistory(ticker, years);
		EXCHANGE exchange = EXCHANGE.London;
		String fileName = exchange.name() + "_" + ticker + ".csv";
		String myCsvText = StockFeed.seriesToCsv(series).toString();
		return Response.ok(myCsvText).header("Content-Disposition", "attachment; filename=" + fileName).build();
	}

	private List<HistoricalQuote> getHistoryData(String ticker, int years) throws IOException {
		EXCHANGE exchange = EXCHANGE.London;
		Optional<Stock> stock = new IntelligentStockFeed().get(exchange, ticker, years);
		if (stock.isPresent()) {
			return stock.get().getHistory();
		}
		return Lists.newArrayList();
	}

}
