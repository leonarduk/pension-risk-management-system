package yahoofinance.quotes.stock;

import java.io.IOException;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;
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

		dividend.setPayDate(DateUtils.parseDividendDate(this.getValue(QuotesProperty.DividendPayDate)));
		dividend.setExDate(DateUtils.parseDividendDate(this.getValue(QuotesProperty.ExDividendDate)));
		dividend.setAnnualYield(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.TrailingAnnualDividendYield)));
		dividend.setAnnualYieldPercent(
				NumberUtils.getBigDecimal(this.getValue(QuotesProperty.TrailingAnnualDividendYieldInPercent)));

		return dividend;
	}

	public StockQuote getQuote() throws IOException {
		final String symbol = this.getValue(QuotesProperty.Symbol);
		final StockQuote quote = new StockQuote(Instrument.fromString(symbol));

		quote.setPrice(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.LastTradePriceOnly)));
		quote.setLastTradeSize(NumberUtils.getLong(this.getValue(QuotesProperty.LastTradeSize)));
		quote.setAsk(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.AskRealtime),
				this.getValue(QuotesProperty.Ask)));
		quote.setAskSize(NumberUtils.getLong(this.getValue(QuotesProperty.AskSize)));
		quote.setBid(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.BidRealtime),
				this.getValue(QuotesProperty.Bid)));
		quote.setBidSize(NumberUtils.getLong(this.getValue(QuotesProperty.BidSize)));
		quote.setOpen(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.Open)));
		quote.setPreviousClose(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.PreviousClose)));
		quote.setDayHigh(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.DaysHigh)));
		quote.setDayLow(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.DaysLow)));

		quote.setTimeZone(ExchangeTimeZone.getStockTimeZone(symbol));
		quote.setLastTradeDateStr(this.getValue(QuotesProperty.LastTradeDate));
		quote.setLastTradeTimeStr(this.getValue(QuotesProperty.LastTradeTime));
		quote.setLastTradeTime(DateUtils.parseDateTime(this.getValue(QuotesProperty.LastTradeDate),
				this.getValue(QuotesProperty.LastTradeTime), quote.getTimeZone()));

		quote.setYearHigh(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.YearHigh)));
		quote.setYearLow(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.YearLow)));
		quote.setPriceAvg50(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.FiftydayMovingAverage)));
		quote.setPriceAvg200(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.TwoHundreddayMovingAverage)));

		quote.setVolume(NumberUtils.getLong(this.getValue(QuotesProperty.Volume)));
		quote.setAvgVolume(NumberUtils.getLong(this.getValue(QuotesProperty.AverageDailyVolume)));

		return quote;
	}

	public StockStats getStats() {
		final String symbol = this.getValue(QuotesProperty.Symbol);
		final StockStats stats = new StockStats(symbol);

		stats.setMarketCap(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.MarketCapitalization)));
		stats.setSharesFloat(NumberUtils.getLong(this.getValue(QuotesProperty.SharesFloat)));
		stats.setSharesOutstanding(NumberUtils.getLong(this.getValue(QuotesProperty.SharesOutstanding)));
		stats.setSharesOwned(NumberUtils.getLong(this.getValue(QuotesProperty.SharesOwned)));

		stats.setEps(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.DilutedEPS)));
		stats.setPe(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.PERatio)));
		stats.setPeg(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.PEGRatio)));

		stats.setEpsEstimateCurrentYear(
				NumberUtils.getBigDecimal(this.getValue(QuotesProperty.EPSEstimateCurrentYear)));
		stats.setEpsEstimateNextQuarter(
				NumberUtils.getBigDecimal(this.getValue(QuotesProperty.EPSEstimateNextQuarter)));
		stats.setEpsEstimateNextYear(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.EPSEstimateNextYear)));

		stats.setPriceBook(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.PriceBook)));
		stats.setPriceSales(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.PriceSales)));
		stats.setBookValuePerShare(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.BookValuePerShare)));

		stats.setOneYearTargetPrice(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.OneyrTargetPrice)));
		stats.setEBITDA(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.EBITDA)));
		stats.setRevenue(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.Revenue)));

		stats.setShortRatio(NumberUtils.getBigDecimal(this.getValue(QuotesProperty.ShortRatio)));

		return stats;
	}

	public Stock getStock() throws IOException {
		final String symbol = this.getValue(QuotesProperty.Symbol);
		final Stock stock = new Stock(Instrument.fromString(symbol.substring(0, symbol.indexOf("."))));

		stock.setCurrency(NumberUtils.getString(this.getValue(QuotesProperty.Currency)));

		stock.setQuote(this.getQuote());
		stock.setStats(this.getStats());
		stock.setDividend(this.getDividend());

		return stock;
	}

	public String getValue(final QuotesProperty property) {
		final int i = StockQuotesRequest.DEFAULT_PROPERTIES.indexOf(property);
		if ((i >= 0) && (i < this.data.length)) {
			return this.data[i];
		}
		return null;
	}

}
