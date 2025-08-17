package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.HistoricalDataService;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Helper class used by AWS components to retrieve historical data and render it as
 * HTML. Parameter parsing and record generation are delegated to
 * {@link HistoricalDataService}.
 */
@Slf4j
public class QueryRunner {
    public static final String INTERPOLATE = "interpolate";
    public static final String CLEAN_DATA = "cleanData";
    public static final String YEARS = "years";
    public static final String TICKER = "ticker";

    private final StockFeed stockFeed;
    private final HistoricalDataService historicalDataService;

    public QueryRunner() {
        this(DependencyFactory.stockFeed());
    }

    public QueryRunner(StockFeed stockFeed) {
        this.stockFeed = stockFeed;
        this.historicalDataService = new HistoricalDataService(stockFeed);
    }

    /**
     * Retrieves results for a given set of parameters.
     *
     * @param inputParams map of parameters – must include {@code ticker}
     * @return HTML representation of the historical data
     * @throws IOException if the underlying feed cannot be accessed
     */
    public String getResults(final Map<String, String> inputParams) throws IOException {
        if (inputParams == null) {
            throw new IllegalArgumentException("No parameters provided. Expect at least ticker");
        }
        log.debug("Input parameters: {}", inputParams);

        List<List<DataField>> records = historicalDataService.getRecords(inputParams);
        StringBuilder sbBody = new StringBuilder();
        HtmlTools.printTable(sbBody, records);
        return HtmlTools.createHtmlText(null, sbBody).toString();
    }
}
