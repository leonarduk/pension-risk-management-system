package yahoofinance.histquotes2;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * All getters can return null in case the data is not available from Yahoo Finance.
 *
 * @author Randle McMurphy
 */
@Setter
@Getter
public class HistoricalSplit {

    private String symbol;

    private Calendar date;

    private BigDecimal numerator;
    private BigDecimal denominator;

    public HistoricalSplit() {
    }

    public HistoricalSplit(String symbol, Calendar date, BigDecimal numerator, BigDecimal denominator) {
        this.symbol = symbol;
        this.date = date;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * @return a calculated split factor value which is equal to numerator divided by denominator
     */
    public BigDecimal getSplitFactor() {
        return numerator.divide(denominator, 10, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(this.date.getTime());
        return "SPLIT: " + this.symbol + "@" + dateStr + ": " + this.numerator + " / " + this.denominator;
    }

}
