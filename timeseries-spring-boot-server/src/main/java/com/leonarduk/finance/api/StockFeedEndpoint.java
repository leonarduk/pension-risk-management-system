package com.leonarduk.finance.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.leonarduk.finance.db.InstrumentRepository;
import com.leonarduk.finance.stockfeed.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.ta4j.core.Bar;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;
import com.leonarduk.finance.utils.TimeseriesUtils;

@Named
@Path("/stock")
@SpringBootApplication
@ComponentScan("com.leonarduk.finance.stockfeed")
public class StockFeedEndpoint {

    private final static Logger logger = LoggerFactory.getLogger(StockFeedEndpoint.class.getName());

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
	private StockFeed stockFeed;

    @Autowired
    private DataStore dataStore;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("instrument")
    public String getInstrument(@QueryParam("ticker") final String ticker) {
        return this.instrumentRepository.findById(ticker).get().toString();
    }

    @GET
    @Path("instruments")
    public String getInstruments() {
        logger.info("JSON query of instruments");
        return this.instrumentRepository.findAll().stream().map(i -> i.toString()).collect(Collectors.toSet()).toString();
    }

    @GET
    @Produces({ MediaType.TEXT_HTML })
    @Path("/ticker/{ticker}")
    public String displayHistoryLondon(@PathParam("ticker") final String ticker,
                                 @QueryParam("years") final int years,
                                 @QueryParam("fromDate") final String fromDate, @QueryParam("toDate") final String toDate,
                                 @QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData,
                                 @QueryParam("fields") final String fields, @QueryParam("addLatest")boolean addLatestQuoteToTheSeries) throws IOException {
        return displayHistory(ticker, "L", years, fromDate, toDate, interpolate, cleanData, fields, addLatestQuoteToTheSeries);
    }

        @GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/ticker/{ticker}/{region}")
	public String displayHistory(@PathParam("ticker") final String ticker,
                                 @PathParam("region") final String region,
                                 @QueryParam("years") final int years,
			@QueryParam("fromDate") final String fromDate, @QueryParam("toDate") final String toDate,
			@QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData,
			@QueryParam("fields") final String fields, @QueryParam("addLatest")boolean addLatestQuoteToTheSeries) throws IOException {

		Instrument instrument = getInstrument(ticker, region);
		String[] fieldArray = {};
		if(fields != null) {
			fieldArray = fields.split(",");
		}
		return generateResults(years, fromDate, toDate, interpolate, cleanData, instrument,
				fieldArray, addLatestQuoteToTheSeries);
	}

    private Instrument getInstrument(String ticker, String region) throws IOException {
        Optional<Instrument> instrument = Optional.empty(); //this.instrumentRepository.findById(ticker);
        if (instrument.isEmpty()){
            instrument = Optional.of(Instrument.fromString(ticker,region));
            this.instrumentRepository.save(instrument.get());
        }

        return instrument.get();
    }



	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/fx/{ccy1}/{ccy2}")
	public String displayFxHistory(@PathParam("ccy1") final String currencyOne,
			@PathParam("ccy2") final String currencyTwo, @QueryParam("years") final int years,
			@QueryParam("fromDate") final String fromDate, @QueryParam("toDate") final String toDate,
			@QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData,
			@QueryParam("fields") final String fields,
                                   @QueryParam("addLatest")boolean addLatestQuoteToTheSeries) throws IOException {

		final Instrument instrument = new FxInstrument(Source.ALPHAVANTAGE, currencyOne, currencyTwo);

		String[] fieldArray = fields.split(",");
		return generateResults(years, fromDate, toDate, interpolate, cleanData, instrument, fieldArray, addLatestQuoteToTheSeries);
	}

	private String generateResults(final int years, final String fromDate, final String toDate,
                                   final boolean interpolate, final boolean cleanData, final Instrument instrument, String[] fields, boolean addLatestQuoteToTheSeries)
			throws IOException {
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

		historyData = this.getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, addLatestQuoteToTheSeries);

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
			@QueryParam("interpolate") final boolean interpolate, @QueryParam("clean") final boolean cleanData, @QueryParam("addLatest")boolean addLatestQuoteToTheSeries)
			throws IOException {
		final Instrument instrument = getInstrument(ticker, "L");
		final List<Bar> series = this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate, cleanData, addLatestQuoteToTheSeries);
		final String fileName = instrument.getExchange().name() + "_" + instrument.code() + ".csv";
		final String myCsvText = TimeseriesUtils.seriesToCsv(series).toString();
		return Response.ok(myCsvText).header("Content-Disposition", "attachment; filename=" + fileName).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/api/ticker/{ticker}/")
	public List<Bar> getHistory(@PathParam("ticker") final String ticker, @QueryParam("years") final int years,
                                @QueryParam("interpolate") final boolean interpolate,
                                @QueryParam("clean") final boolean cleanData,
                                @QueryParam("addLatest")boolean addLatestQuoteToTheSeries)
			throws IOException {
		final Instrument instrument = getInstrument(ticker, "L");
		return this.getHistoryData(instrument, years == 0 ? 1 : years, interpolate, cleanData, addLatestQuoteToTheSeries);
	}

	private List<Bar> getHistoryData(final Instrument instrument, final int years, final boolean interpolate,
                                     boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException {
		return getHistoryData(instrument, LocalDate.now().plusYears(-1 * years), LocalDate.now(), interpolate,
				cleanData, addLatestQuoteToTheSeries);
	}

	private List<Bar> getHistoryData(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate,
                                     boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException {
		final Optional<StockV1> stock = this.stockFeed.get(instrument, fromLocalDate, toLocalDate, interpolate,
				cleanData, addLatestQuoteToTheSeries);
		if (stock.isPresent()) {
			return stock.get().getHistory();
		}
		return Lists.newArrayList();
	}

}
