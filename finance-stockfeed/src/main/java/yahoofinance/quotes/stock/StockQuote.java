
package yahoofinance.quotes.stock;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.NumberUtils;

/**
 * All getters can return null in case the data is not available from Yahoo Finance.
 *
 * @author Stijn Strickx
 */
public class StockQuote {

	private final BigDecimal	ask;

	private final Long			askSize;

	private final Long			avgVolume;

	private final BigDecimal	bid;
	private final Long			bidSize;
	private final BigDecimal	dayHigh;
	private final BigDecimal	dayLow;
	private final Instrument	instrument;

	private final String		lastTradeDateStr;
	private final Long			lastTradeSize;
	private final Calendar		lastTradeTime;
	private final String		lastTradeTimeStr;

	private final BigDecimal	open;
	private final BigDecimal	previousClose;
	private final BigDecimal	price;
	private final BigDecimal	priceAvg200;

	private final BigDecimal	priceAvg50;
	private final TimeZone		timeZone;
	private final Long			volume;
	private final BigDecimal	yearHigh;

	private final BigDecimal	yearLow;

	public static class StockQuoteBuilder {
		private BigDecimal			ask;

		private Long				askSize;

		private Long				avgVolume;

		private BigDecimal			bid;
		private Long				bidSize;
		private BigDecimal			dayHigh;
		private BigDecimal			dayLow;
		private final Instrument	instrument;

		private String				lastTradeDateStr;
		private Long				lastTradeSize;
		private Calendar			lastTradeTime;
		private String				lastTradeTimeStr;

		private BigDecimal			open;
		private BigDecimal			previousClose;
		private BigDecimal			price;
		private BigDecimal			priceAvg200;

		private BigDecimal			priceAvg50;
		private TimeZone			timeZone;
		private Long				volume;
		private BigDecimal			yearHigh;

		private BigDecimal			yearLow;

		public StockQuoteBuilder(final Instrument instrument) {
			this.instrument = instrument;
		}

		public StockQuote build() {
			return new StockQuote(this.ask, this.askSize, this.avgVolume, this.bid, this.bidSize,
			        this.dayHigh, this.dayLow, this.instrument, this.lastTradeDateStr,
			        this.lastTradeSize, this.lastTradeTime, this.lastTradeTimeStr, this.open,
			        this.previousClose, this.price, this.priceAvg200, this.priceAvg50,
			        this.timeZone, this.volume, this.yearHigh, this.yearLow);
		}

		public StockQuoteBuilder setAsk(final BigDecimal ask) {
			this.ask = ask;
			return this;
		}

		public StockQuoteBuilder setAskSize(final Long askSize) {
			this.askSize = askSize;
			return this;
		}

		public StockQuoteBuilder setAvgVolume(final Long avgVolume) {
			this.avgVolume = avgVolume;
			return this;
		}

		public StockQuoteBuilder setBid(final BigDecimal bid) {
			this.bid = bid;
			return this;
		}

		public StockQuoteBuilder setBidSize(final Long bidSize) {
			this.bidSize = bidSize;
			return this;
		}

		public StockQuoteBuilder setDayHigh(final BigDecimal dayHigh) {
			this.dayHigh = dayHigh;
			return this;
		}

		public StockQuoteBuilder setDayLow(final BigDecimal dayLow) {
			this.dayLow = dayLow;
			return this;
		}

		public StockQuoteBuilder setLastTradeDateStr(final String lastTradeDateStr) {
			this.lastTradeDateStr = lastTradeDateStr;
			return this;
		}

		public StockQuoteBuilder setLastTradeSize(final Long lastTradeSize) {
			this.lastTradeSize = lastTradeSize;
			return this;
		}

		public StockQuoteBuilder setLastTradeTime(final Calendar lastTradeTime) {
			this.lastTradeTime = lastTradeTime;
			return this;
		}

		public StockQuoteBuilder setLastTradeTimeStr(final String lastTradeTimeStr) {
			this.lastTradeTimeStr = lastTradeTimeStr;
			return this;
		}

		public StockQuoteBuilder setOpen(final BigDecimal open) {
			this.open = open;
			return this;
		}

		public StockQuoteBuilder setPreviousClose(final BigDecimal previousClose) {
			this.previousClose = previousClose;
			return this;
		}

		public StockQuoteBuilder setPrice(final BigDecimal price) {
			this.price = price;
			return this;
		}

		public StockQuoteBuilder setPriceAvg200(final BigDecimal priceAvg200) {
			this.priceAvg200 = priceAvg200;
			return this;
		}

		public StockQuoteBuilder setPriceAvg50(final BigDecimal priceAvg50) {
			this.priceAvg50 = priceAvg50;
			return this;
		}

		public StockQuoteBuilder setTimeZone(final TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public StockQuoteBuilder setVolume(final Long volume) {
			this.volume = volume;
			return this;
		}

		public StockQuoteBuilder setYearHigh(final BigDecimal yearHigh) {
			this.yearHigh = yearHigh;
			return this;
		}

		public StockQuoteBuilder setYearLow(final BigDecimal yearLow) {
			this.yearLow = yearLow;
			return this;
		}
	}

	public StockQuote(final BigDecimal ask, final Long askSize, final Long avgVolume,
	        final BigDecimal bid, final Long bidSize, final BigDecimal dayHigh,
	        final BigDecimal dayLow, final Instrument instrument, final String lastTradeDateStr,
	        final Long lastTradeSize, final Calendar lastTradeTime, final String lastTradeTimeStr,
	        final BigDecimal open, final BigDecimal previousClose, final BigDecimal price,
	        final BigDecimal priceAvg200, final BigDecimal priceAvg50, final TimeZone timeZone,
	        final Long volume, final BigDecimal yearHigh, final BigDecimal yearLow) {
		this.ask = ask;
		this.askSize = askSize;
		this.avgVolume = avgVolume;
		this.bid = bid;
		this.bidSize = bidSize;
		this.dayHigh = dayHigh;
		this.dayLow = dayLow;
		this.instrument = instrument;
		this.lastTradeDateStr = lastTradeDateStr;
		this.lastTradeSize = lastTradeSize;
		this.lastTradeTime = lastTradeTime;
		this.lastTradeTimeStr = lastTradeTimeStr;
		this.open = open;
		this.previousClose = previousClose;
		this.price = price;
		this.priceAvg200 = priceAvg200;
		this.priceAvg50 = priceAvg50;
		this.timeZone = timeZone;
		this.volume = volume;
		this.yearHigh = yearHigh;
		this.yearLow = yearLow;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StockQuote)) {
			return false;
		}
		final StockQuote castOther = (StockQuote) other;
		return new EqualsBuilder().append(this.ask, castOther.ask)
		        .append(this.askSize, castOther.askSize).append(this.avgVolume, castOther.avgVolume)
		        .append(this.bid, castOther.bid).append(this.bidSize, castOther.bidSize)
		        .append(this.dayHigh, castOther.dayHigh).append(this.dayLow, castOther.dayLow)
		        .append(this.instrument, castOther.instrument)
		        .append(this.lastTradeDateStr, castOther.lastTradeDateStr)
		        .append(this.lastTradeSize, castOther.lastTradeSize)
		        .append(this.lastTradeTime, castOther.lastTradeTime)
		        .append(this.lastTradeTimeStr, castOther.lastTradeTimeStr)
		        .append(this.open, castOther.open)
		        .append(this.previousClose, castOther.previousClose)
		        .append(this.price, castOther.price).append(this.priceAvg200, castOther.priceAvg200)
		        .append(this.priceAvg50, castOther.priceAvg50)
		        .append(this.timeZone, castOther.timeZone).append(this.volume, castOther.volume)
		        .append(this.yearHigh, castOther.yearHigh).append(this.yearLow, castOther.yearLow)
		        .isEquals();
	}

	public BigDecimal getAsk() {
		return this.ask;
	}

	public Long getAskSize() {
		return this.askSize;
	}

	public Long getAvgVolume() {
		return this.avgVolume;
	}

	public BigDecimal getBid() {
		return this.bid;
	}

	public Long getBidSize() {
		return this.bidSize;
	}

	/**
	 *
	 * @return difference between current price and previous close
	 */
	public BigDecimal getChange() {
		if ((this.price == null) || (this.previousClose == null)) {
			return null;
		}
		return this.price.subtract(this.previousClose);
	}

	/**
	 *
	 * @return difference between current price and 200 day moving average
	 */
	public BigDecimal getChangeFromAvg200() {
		if ((this.price == null) || (this.priceAvg200 == null)) {
			return null;
		}
		return this.price.subtract(this.priceAvg200);
	}

	/**
	 *
	 * @return change from 200 day moving average relative to 200 day moving average
	 */
	public BigDecimal getChangeFromAvg200InPercent() {
		return NumberUtils.getPercent(this.getChangeFromAvg200(), this.priceAvg200);
	}

	/**
	 *
	 * @return difference between current price and 50 day moving average
	 */
	public BigDecimal getChangeFromAvg50() {
		if ((this.price == null) || (this.priceAvg50 == null)) {
			return null;
		}
		return this.price.subtract(this.priceAvg50);
	}

	/**
	 *
	 * @return change from 50 day moving average relative to 50 day moving average
	 */
	public BigDecimal getChangeFromAvg50InPercent() {
		return NumberUtils.getPercent(this.getChangeFromAvg50(), this.priceAvg50);
	}

	/**
	 *
	 * @return difference between current price and year high
	 */
	public BigDecimal getChangeFromYearHigh() {
		if ((this.price == null) || (this.yearHigh == null)) {
			return null;
		}
		return this.price.subtract(this.yearHigh);
	}

	/**
	 *
	 * @return change from year high relative to year high
	 */
	public BigDecimal getChangeFromYearHighInPercent() {
		return NumberUtils.getPercent(this.getChangeFromYearHigh(), this.yearHigh);
	}

	/**
	 *
	 * @return difference between current price and year low
	 */
	public BigDecimal getChangeFromYearLow() {
		if ((this.price == null) || (this.yearLow == null)) {
			return null;
		}
		return this.price.subtract(this.yearLow);
	}

	/**
	 *
	 * @return change from year low relative to year low
	 */
	public BigDecimal getChangeFromYearLowInPercent() {
		return NumberUtils.getPercent(this.getChangeFromYearLow(), this.yearLow);
	}

	/**
	 *
	 * @return change relative to previous close
	 */
	public BigDecimal getChangeInPercent() {
		return NumberUtils.getPercent(this.getChange(), this.previousClose);
	}

	public BigDecimal getDayHigh() {
		return this.dayHigh;
	}

	public BigDecimal getDayLow() {
		return this.dayLow;
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

	public String getLastTradeDateStr() {
		return this.lastTradeDateStr;
	}

	public Long getLastTradeSize() {
		return this.lastTradeSize;
	}

	/**
	 * Will derive the time zone from the exchange to parse the date time into a Calendar object.
	 * This will not react to changes in the lastTradeDateStr and lastTradeTimeStr
	 *
	 * @return last trade date time
	 */
	public Calendar getLastTradeTime() {
		return this.lastTradeTime;
	}

	/**
	 * Will use the provided time zone to parse the date time into a Calendar object Reacts to
	 * changes in the lastTradeDateStr and lastTradeTimeStr
	 *
	 * @param timeZone
	 *            time zone where the stock is traded
	 * @return last trade date time
	 */
	public Calendar getLastTradeTime(final TimeZone timeZone) {
		return DateUtils.parseDateTime(this.lastTradeDateStr, this.lastTradeTimeStr, timeZone);
	}

	public String getLastTradeTimeStr() {
		return this.lastTradeTimeStr;
	}

	public BigDecimal getOpen() {
		return this.open;
	}

	public BigDecimal getPreviousClose() {
		return this.previousClose;
	}

	public BigDecimal getPrice() {
		if (null == this.price) {
			return BigDecimal.ZERO;
		}
		return this.price;
	}

	/**
	 *
	 * @return 200 day moving average
	 */
	public BigDecimal getPriceAvg200() {
		return this.priceAvg200;
	}

	/**
	 *
	 * @return 50 day moving average
	 */
	public BigDecimal getPriceAvg50() {
		return this.priceAvg50;
	}

	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public Long getVolume() {
		return this.volume;
	}

	public BigDecimal getYearHigh() {
		return this.yearHigh;
	}

	public BigDecimal getYearLow() {
		return this.yearLow;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.ask).append(this.askSize).append(this.avgVolume)
		        .append(this.bid).append(this.bidSize).append(this.dayHigh).append(this.dayLow)
		        .append(this.instrument).append(this.lastTradeDateStr).append(this.lastTradeSize)
		        .append(this.lastTradeTime).append(this.lastTradeTimeStr).append(this.open)
		        .append(this.previousClose).append(this.price).append(this.priceAvg200)
		        .append(this.priceAvg50).append(this.timeZone).append(this.volume)
		        .append(this.yearHigh).append(this.yearLow).toHashCode();
	}

	public boolean isPopulated() {
		return this.lastTradeDateStr != null;
	}

	@Override
	public String toString() {
		return "Ask: " + this.ask + ", Bid: " + this.bid + ", Price: " + this.price
		        + ", Prev close: " + this.previousClose;
	}
}
