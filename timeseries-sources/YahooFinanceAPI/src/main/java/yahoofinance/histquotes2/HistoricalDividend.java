package yahoofinance.histquotes2;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * At the time of this writing Yahoo returns ADJUSTED dividends. Which means that as soon as
 * split occurs, all past dividends are divided by split factor.
 * All getters can return null in case the data is not available from Yahoo Finance.
 *
 * @author Randle McMurphy
 */
@Setter
@Getter
public class HistoricalDividend {

    private String symbol;

    private Calendar date;

    /**
     * -- GETTER --
     *  At the time of this writing Yahoo returns ADJUSTED dividends. Which means that as soon as
     *  split occurs, all past dividends are divided by split factor.
     *
     */
    private BigDecimal adjDividend;

    public HistoricalDividend() {
    }

    public HistoricalDividend(String symbol, Calendar date, BigDecimal adjDividend) {
        this.symbol = symbol;
        this.date = date;
        this.adjDividend = adjDividend;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(this.date.getTime());
        return "DIVIDEND: " + this.symbol + "@" + dateStr + ": " + this.adjDividend;
    }
}
