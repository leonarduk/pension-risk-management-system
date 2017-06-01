package yahoofinance.quotes.stock;

import java.io.IOException;
import java.util.TimeZone;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.NumberUtils;

import yahoofinance.exchanges.ExchangeTimeZone;
import yahoofinance.quotes.QuotesProperty;

/**
 *
 * @author Stijn Strickx
 */
public class StockQuotesData {

	private final String[] data;

	public StockQuotesData(final String[] data) {
		this.data = data;
	}

	public StockDividend getDividend() {
		final String symbol = this.getValue(QuotesProperty.Symbol);
		final StockDividend dividend = new StockDividend(symbol);

		dividend.setPayDate(DateUtils.parseDividendDate(
		        this.getValue(QuotesProperty.DividendPayDate)));
		dividend.setExDate(DateUtils.parseDividendDate(
		        this.getValue(QuotesProperty.ExDividendDate)));
		dividend.setAnnualYield(NumberUtils.getBigDecimal(
		        this.getValue(QuotesProperty.TrailingAnnualDividendYield)));
		dividend.setAnnualYieldPercent(NumberUtils.getBigDecimal(this.getValue(
		        QuotesProperty.TrailingAnnualDividendYieldInPercent)));

		return dividend;
	}

	public StockQuote getQuote() throws IOException {
		final String symbol = this.getValue(QuotesProperty.Symbol);
		final TimeZone stockTimeZone = ExchangeTimeZone
		        .getStockTimeZone(symbol);
		return new StockQuote.StockQuoteBuilder(Instrument.fromString(symbol))
		        .setPrice(NumberUtils.getBigDecimal(
		                this.getValue(QuotesProperty.LastTradePriceOnly)))
		        .setLastTradeSize(NumberUtils
		                .getLong(this.getValue(QuotesProperty.LastTradeSize)))
		        .setAsk(NumberUtils.getBigDecimal(
		                this.getValue(QuotesProperty.AskRealtime),
		                this.getValue(QuotesProperty.Ask)))
		        .setAskSize(NumberUtils
		                .getLong(this.getValue(QuotesProperty.AskSize)))
		        .setBid(NumberUtils.getBigDecimal(
		                this.getValue(QuotesProperty.BidRealtime),
		                this.getValue(QuotesProperty.Bid)))
		        .setBidSize(NumberUtils
		                .getLong(this.getValue(QuotesProperty.BidSize)))
		        .setOpen(NumberUtils
		                .getBigDecimal(this.getValue(QuotesProperty.Open)))
		        .setPreviousClose(NumberUtils.getBigDecimal(
		                this.getValue(QuotesProperty.PreviousClose)))
		        .setDayHigh(NumberUtils
		                .getBigDecimal(this.getValue(QuotesProperty.DaysHigh)))
		        .setDayLow(NumberUtils
		                .getBigDecimal(this.getValue(QuotesProperty.DaysLow)))
		        .setTimeZone(stockTimeZone)
		        .setLastTradeDateStr(
		                this.getValue(QuotesProperty.LastTradeDate))
		        .setLastTradeTimeStr(
		                this.getValue(QuotesProperty.LastTradeTime))
		        .setLastTradeTime(DateUtils.parseDateTime(
		                this.getValue(QuotesProperty.LastTradeDate),
		                this.getValue(QuotesProperty.LastTradeTime),
		                stockTimeZone))
		        .setYearHigh(NumberUtils
		                .getBigDecimal(this.getValue(QuotesProperty.YearHigh)))
		        .setYearLow(NumberUtils
		                .getBigDecimal(this.getValue(QuotesProperty.YearLow)))
		        .setPriceAvg50(NumberUtils.getBigDecimal(
		                this.getValue(QuotesProperty.FiftydayMovingAverage)))
		        .setPriceAvg200(NumberUtils.getBigDecimal(this
		                .getValue(QuotesProperty.TwoHundreddayMovingAverage)))

		        .setVolume(NumberUtils
		                .getLong(this.getValue(QuotesProperty.Volume)))
		        .setAvgVolume(NumberUtils.getLong(
		                this.getValue(QuotesProperty.AverageDailyVolume)))
		        .build();
	}

	public String getValue(final QuotesProperty property) {
		final int i = StockQuotesRequest.DEFAULT_PROPERTIES.indexOf(property);
		if ((i >= 0) && (i < this.data.length)) {
			return this.data[i];
		}
		return null;
	}

}
