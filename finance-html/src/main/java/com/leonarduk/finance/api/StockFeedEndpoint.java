package com.leonarduk.finance.api;

import java.io.IOException;
import java.time.LocalDate;
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

import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;
import com.leonarduk.finance.utils.TimeseriesUtils;

@Named
@Path("/stock")
public class StockFeedEndpoint {
	StockFeed stockFeed;

	public StockFeedEndpoint() {
		this(new IntelligentStockFeed());
	}

	public StockFeedEndpoint(final StockFeed stockFeed) {
		this.stockFeed = stockFeed;
	}

	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/ticker/{ticker}/")
	public String displayHistory(@PathParam("ticker") final String ticker, @QueryParam("years") final int years,
			@QueryParam("fromDate") final String fromDate, @QueryParam("toDate") final String toDate,
			@QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData)
			throws IOException {

		final Instrument instrument = Instrument.fromString(ticker);
		final StringBuilder sbBody = new StringBuilder();
		final List<List<DataField>> records = Lists.newArrayList();

		final List<Bar> historyData;
		LocalDate toLocalDate;
		final LocalDate fromLocalDate;

		if (!StringUtils.isEmpty(fromDate)) {
			fromLocalDate = LocalDate.parse(fromDate);
			if (StringUtils.isEmpty(toDate)) {
				toLocalDate = LocalDate.now();
			} else {
				toLocalDate = LocalDate.parse(toDate);
			}

		} else {
			toLocalDate = LocalDate.now();
			fromLocalDate = LocalDate.now().plusYears(-1 * years);
		}

		historyData = this.getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData);

		for (final Bar historicalQuote : historyData) {
			final ArrayList<DataField> record = Lists.newArrayList();
			records.add(record);
			record.add(new DataField("Date", historicalQuote.getEndTime().toLocalDate().toString()));
			record.add(new DataField("Open", historicalQuote.getOpenPrice()));
			record.add(new DataField("High", historicalQuote.getMaxPrice()));
			record.add(new DataField("Low", historicalQuote.getMinPrice()));
			record.add(new DataField("Close", historicalQuote.getClosePrice()));
			record.add(new DataField("Volume", historicalQuote.getVolume()));

			if (historicalQuote instanceof Commentable) {
				Commentable commentable = (Commentable) historicalQuote;
				record.add(new DataField("Comment", commentable.getComment()));

			}
		}

		HtmlTools.printTable(sbBody, records);
		return HtmlTools.createHtmlText(null, sbBody).toString();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/download/ticker/{ticker}/")
	public Response downloadHistoryCsv(@PathParam("ticker") final String ticker, @QueryParam("years") final int years,
			@QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData)
			throws IOException {
		final Instrument instrument = Instrument.fromString(ticker);
		final List<Bar> series = this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate, cleanData);
		final String fileName = instrument.getExchange().name() + "_" + instrument.code() + ".csv";
		final String myCsvText = TimeseriesUtils.seriesToCsv(series).toString();
		return Response.ok(myCsvText).header("Content-Disposition", "attachment; filename=" + fileName).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/api/ticker/{ticker}/")
	public List<Bar> getHistory(@PathParam("ticker") final String ticker, @QueryParam("years") final int years,
			@QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData)
			throws IOException {
		final Instrument instrument = Instrument.fromString(ticker);
		return this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate, cleanData);
	}

	private List<Bar> getHistoryData(final Instrument instrument, final int years, final boolean interpolate,
			boolean cleanData) throws IOException {
		return getHistoryData(instrument, LocalDate.now().plusYears(-1 * years), LocalDate.now(), interpolate,
				cleanData);
	}

	private List<Bar> getHistoryData(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate,
			boolean interpolate, boolean cleanData) throws IOException {
//		while (toLocalDate.getDayOfWeek().ordinal() > DayOfWeek.FRIDAY.ordinal()) {
//			toLocalDate = toLocalDate.minusDays(1);
//		}
		final Optional<StockV1> stock = this.stockFeed.get(instrument, fromLocalDate, toLocalDate, interpolate,
				cleanData);
		if (stock.isPresent()) {
			return stock.get().getHistory();
		}
		return Lists.newArrayList();
	}

}
