package com.leonarduk.finance.springboot;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.leonarduk.finance.stockfeed.*;
import com.leonarduk.finance.stockfeed.HistoricalDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;

/**
 * REST endpoint for accessing stock feed data.
 */
@RequestMapping("/stock")
@RestController
public class StockFeedEndpoint {

    @Autowired
    private final StockFeed stockFeed;

    private final HistoricalDataService historicalDataService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Constructor for dependency injection.
     *
     * @param stockFeed the stock feed service
     */
    public StockFeedEndpoint(StockFeed stockFeed) {
        this.stockFeed = stockFeed;
        this.historicalDataService = new HistoricalDataService(stockFeed);
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
     * @param category optional instrument category filter
     * @return HTML table with historical stock data
     * @throws IOException if data retrieval fails
     */
    @GetMapping(value = "/ticker/{ticker}", produces = MediaType.TEXT_HTML_VALUE)
    public String displayHistory(@PathVariable(name = "ticker") final String ticker,
                                 @RequestParam(name = "years", required = false) Integer years,
                                 @RequestParam(name = "fromDate", required = false) String fromDate,
                                 @RequestParam(name = "toDate", required = false) String toDate,
                                 @RequestParam(name = "fields", required = false) String fields,
                                 @RequestParam(name = "scaling", required = false) Double scaling,
                                 @RequestParam(name = "interpolate", required = false) boolean interpolate,
                                 @RequestParam(name = "cleanDate", required = false) boolean cleanDate,
                                 @RequestParam(name = "category", required = false) String category,
                              @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage,
                                 @RequestParam(name = "lang", required = false) String lang
    ) throws IOException {

        Locale locale = Locale.getDefault();
        if (StringUtils.isNotBlank(lang)) {
            locale = Locale.forLanguageTag(lang);
        } else if (StringUtils.isNotBlank(acceptLanguage)) {
            locale = Locale.forLanguageTag(acceptLanguage);
        }
        LocaleContextHolder.setLocale(locale);
        Locale.setDefault(locale);

        List<List<DataField>> records;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("ticker", ticker);
            if (years != null) params.put("years", years.toString());
            if (fromDate != null) params.put("fromDate", fromDate);
            if (toDate != null) params.put("toDate", toDate);
            if (scaling != null) params.put("scaling", scaling.toString());
            params.put("interpolate", Boolean.toString(interpolate));
            params.put("cleanData", Boolean.toString(cleanDate));
            if (category != null) params.put("category", category);
            records = historicalDataService.getRecords(params);
        } catch (StockFeedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        }

        final StringBuilder sbBody = new StringBuilder();
        String heading = messageSource.getMessage("stock.title", new Object[]{ticker}, locale);
        sbBody.append("<h1>").append(heading).append("</h1>");
        HtmlTools.printTable(sbBody, records);
        return HtmlTools.createHtmlText(null, sbBody).toString();
    }

    @PostMapping("/ticker")
    @ResponseBody
    public Map<String, Map<String, Double>> displayHistoryAsJson(@RequestParam("ticker") final String tickerArg,
                                                                 @RequestParam(name = "years", required = false) Integer years,
                                                                 @RequestParam(name = "fromDate", required = false) String fromDate,
                                                                 @RequestParam(name = "toDate", required = false) String toDate,
                                                                 @RequestParam(name = "fields", required = false) String fields,
                                                                 @RequestParam(name = "scaling", required = false) Double scaling,
                                                                 @RequestParam(name = "interpolate", required = false) boolean interpolate,
                                                                 @RequestParam(name = "cleanDate", required = false) boolean cleanDate,
                                                                 @RequestParam(name = "category", required = false) String category,
                                                                 @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage,
                                                                 @RequestParam(name = "lang", required = false) String lang
    ) throws IOException {
        Locale locale = Locale.getDefault();
        if (StringUtils.isNotBlank(lang)) {
            locale = Locale.forLanguageTag(lang);
        } else if (StringUtils.isNotBlank(acceptLanguage)) {
            locale = Locale.forLanguageTag(acceptLanguage);
        }
        LocaleContextHolder.setLocale(locale);
        Locale.setDefault(locale);

        Map<String, Map<String, Double>> result = new TreeMap<>();

        List<String> tickers = new ArrayList<>();
        if (tickerArg.contains(",")) {
            String[] tickerArray = tickerArg.split(",");
            Collections.addAll(tickers, tickerArray);
        } else {
            tickers.add(tickerArg);
        }

        try {
            for (String ticker : tickers) {
                Map<String, String> params = new HashMap<>();
                params.put("ticker", ticker);
                if (years != null) params.put("years", years.toString());
                if (fromDate != null) params.put("fromDate", fromDate);
                if (toDate != null) params.put("toDate", toDate);
                if (scaling != null) params.put("scaling", scaling.toString());
                params.put("interpolate", Boolean.toString(interpolate));
                params.put("cleanData", Boolean.toString(cleanDate));
                if (category != null) params.put("category", category);

                List<List<DataField>> records = historicalDataService.getRecords(params);
                if (records.isEmpty()) {
                    continue;
                }
                Map<String, Double> datePriceMap = new TreeMap<>();

                for (List<DataField> record : records) {
                    String date = null;
                    Double closePrice = null;

                    for (DataField field : record) {
                        if ("Date".equals(field.getName())) {
                            date = field.getValue().toString();
                        } else if ("Close".equals(field.getName())) {
                            closePrice = Double.valueOf(field.getValue().toString());
                        }
                    }

                    if (date != null && closePrice != null) {
                        datePriceMap.put(date, closePrice);
                    }
                }

                result.put(ticker, datePriceMap);
            }
        } catch (StockFeedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Return the latest closing price for the supplied ticker.
     *
     * @param ticker the stock ticker
     * @return a JSON map containing the close price
     * @throws IOException if the stock data cannot be retrieved
     */
    @GetMapping("/price/{ticker}")
    @ResponseBody
    public Map<String, BigDecimal> getLatestClosePrice(
            @PathVariable(name = "ticker") final String ticker) throws IOException {
        Instrument instrument = Instrument.fromString(ticker);
        try {
            Optional<StockV1> stock = stockFeed.get(instrument, 1, true);
            return stock
                    .map(s -> Collections.singletonMap("close", s.getQuote().getPrice()))
                    .orElse(Collections.emptyMap());
        } catch (StockFeedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        }
    }

}
