
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

	public StockQuote(final Instrument instrument) {
		this.instrument = instrument;
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

	public void setAsk(final BigDecimal ask) {
		this.ask = ask;
	}

	public void setAskSize(final Long askSize) {
		this.askSize = askSize;
	}

	public void setAvgVolume(final Long avgVolume) {
		this.avgVolume = avgVolume;
	}

	public void setBid(final BigDecimal bid) {
		this.bid = bid;
	}

	public void setBidSize(final Long bidSize) {
		this.bidSize = bidSize;
	}

	public void setDayHigh(final BigDecimal dayHigh) {
		this.dayHigh = dayHigh;
	}

	public void setDayLow(final BigDecimal dayLow) {
		this.dayLow = dayLow;
	}

	public void setLastTradeDateStr(final String lastTradeDateStr) {
		this.lastTradeDateStr = lastTradeDateStr;
	}

	public void setLastTradeSize(final Long lastTradeSize) {
		this.lastTradeSize = lastTradeSize;
	}

	public void setLastTradeTime(final Calendar lastTradeTime) {
		this.lastTradeTime = lastTradeTime;
	}

	public void setLastTradeTimeStr(final String lastTradeTimeStr) {
		this.lastTradeTimeStr = lastTradeTimeStr;
	}

	public void setOpen(final BigDecimal open) {
		this.open = open;
	}

	public void setPreviousClose(final BigDecimal previousClose) {
		this.previousClose = previousClose;
	}

	public void setPrice(final BigDecimal price) {
		this.price = price;
	}

	public void setPriceAvg200(final BigDecimal priceAvg200) {
		this.priceAvg200 = priceAvg200;
	}

	public void setPriceAvg50(final BigDecimal priceAvg50) {
		this.priceAvg50 = priceAvg50;
	}

	public void setTimeZone(final TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public void setVolume(final Long volume) {
		this.volume = volume;
	}

	public void setYearHigh(final BigDecimal yearHigh) {
		this.yearHigh = yearHigh;
	}

	public void setYearLow(final BigDecimal yearLow) {
		this.yearLow = yearLow;
	}

	@Override
	public String toString() {
		return "Ask: " + this.ask + ", Bid: " + this.bid + ", Price: " + this.price
		        + ", Prev close: " + this.previousClose;
	}

}
