package com.leonarduk.finance.springboot;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.leonarduk.finance.stockfeed.*;
import com.leonarduk.finance.stockfeed.datatransformation.correction.ValueScalingTransformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.ta4j.core.Bar;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;

/**
 * REST endpoint for accessing stock feed data.
 */
@RequestMapping("/stock")
@RestController
public class StockFeedEndpoint {

    @Autowired
    private final StockFeed stockFeed;

    /**
     * Constructor for dependency injection.
     *
     * @param stockFeed the stock feed service
     */
    public StockFeedEndpoint(StockFeed stockFeed) {
        this.stockFeed = stockFeed;
    }

    /**
     * Simple hello world endpoint.
     *
     * @param ticker the stock ticker
     * @return a static greeting message
     */
    @GetMapping("/hello/{ticker}")
    @ResponseBody
    public String hello(@PathVariable("ticker") final String ticker) {
        return "Hello, World!";
    }

    /**
     * Display stock history data in HTML format.
     *
     * @param ticker the stock ticker
     * @param years optional number of years to look back
     * @param fromDate optional start date (yyyy-MM-dd)
     * @param toDate optional end date (yyyy-MM-dd)
     * @param fields optional comma-separated fields
     * @param scaling optional scaling factor for values
     * @param interpolate whether to interpolate missing data
     * @param cleanDate whether to clean non-trading days
     * @return HTML table with historical stock data
     * @throws IOException if data retrieval fails
     */
    @GetMapping("/ticker/{ticker}")
    public String displayHistory(@PathVariable("ticker") final String ticker,
                                 @RequestParam(name = "years", required = false) Integer years,
                                 @RequestParam(name = "fromDate", required = false) String fromDate,
                                 @RequestParam(name = "toDate", required = false) String toDate,
                                 @RequestParam(name = "fields", required = false) String fields,
                                 @RequestParam(name = "scaling", required = false) Double scaling,
                                 @RequestParam(name = "interpolate", required = false) boolean interpolate,
                                 @RequestParam(name = "cleanDate", required = false) boolean cleanDate
    ) throws IOException {

        Instrument instrument = Instrument.fromString(ticker);

        String[] fieldArray = {};
        if (fields != null) {
            fieldArray = fields.split(",");
        }

        List<List<DataField>> records = generateResults(years, fromDate, toDate, instrument,
            fieldArray, interpolate, cleanDate, scaling);

        final StringBuilder sbBody = new StringBuilder();
        HtmlTools.printTable(sbBody, records);
        return HtmlTools.createHtmlText(null, sbBody).toString();
    }

    /**
     * Generate historical stock data records.
     *
     * @param years number of years to go back
     * @param fromDate start date string
     * @param toDate end date string
     * @param instrument the stock instrument
     * @param fields fields to include in output
     * @param interpolate whether to interpolate data
     * @param cleanDate whether to remove non-trading days
     * @param scaling optional value scaling factor
     * @return list of historical data records
     * @throws IOException if data fetch fails
     */
    private List<List<DataField>> generateResults(Integer years,
                                                  final String fromDate,
                                                  final String toDate,
                                                  final Instrument instrument,
                                                  String[] fields,
                                                  boolean interpolate,
                                                  boolean cleanDate,
                                                  Double scaling)
        throws IOException {

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
            if (years == null) {
                years = 10;
            }
            toLocalDate = LocalDate.now();
            fromLocalDate = LocalDate.now().plusYears(-1L * years);
        }

        historyData = this.getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanDate, scaling);

        for (final Bar historicalQuote : historyData) {
            final ArrayList<DataField> record = Lists.newArrayList();
            records.add(record);
            record.add(new DataField("Date", historicalQuote.getEndTime().toLocalDate().toString()));
            record.add(new DataField("Open", historicalQuote.getOpenPrice()));
            record.add(new DataField("High", historicalQuote.getMaxPrice()));
            record.add(new DataField("Low", historicalQuote.getMinPrice()));
            record.add(new DataField("Close", historicalQuote.getClosePrice()));
            record.add(new DataField("Volume", historicalQuote.getVolume()));

            if (historicalQuote instanceof Commentable commentable) {
                record.add(new DataField("Comment", commentable.getComment()));
            }
        }
        return records;
    }

    /**
     * Fetches historical stock data.
     *
     * @param instrument the stock instrument
     * @param fromLocalDate start date
     * @param toLocalDate end date
     * @param interpolate whether to interpolate data
     * @param clearData whether to clear non-trading days
     * @param scaling optional value scaling
     * @return list of bars (stock history data)
     * @throws IOException if data fetch fails
     */
    private List<Bar> getHistoryData(Instrument instrument,
                                     LocalDate fromLocalDate,
                                     LocalDate toLocalDate,
                                     boolean interpolate,
                                     boolean clearData,
                                     Double scaling) throws IOException {
        final Optional<StockV1> stock = this.stockFeed.get(instrument,
            fromLocalDate, toLocalDate, interpolate, clearData,
            false);

        if (stock.isPresent()) {
            List<Bar> history = stock.get().getHistory();
            if (scaling != null) {
                return new ValueScalingTransformer(instrument, scaling).clean(history);
            }
            return history;
        }
        return Lists.newArrayList();
    }

}
