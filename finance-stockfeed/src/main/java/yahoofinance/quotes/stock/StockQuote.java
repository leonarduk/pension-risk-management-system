
package yahoofinance.quotes.stock;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final StockQuote other = (StockQuote) obj;
		if (this.ask == null) {
			if (other.ask != null) {
				return false;
			}
		}
		else if (!this.ask.equals(other.ask)) {
			return false;
		}
		if (this.askSize == null) {
			if (other.askSize != null) {
				return false;
			}
		}
		else if (!this.askSize.equals(other.askSize)) {
			return false;
		}
		if (this.avgVolume == null) {
			if (other.avgVolume != null) {
				return false;
			}
		}
		else if (!this.avgVolume.equals(other.avgVolume)) {
			return false;
		}
		if (this.bid == null) {
			if (other.bid != null) {
				return false;
			}
		}
		else if (!this.bid.equals(other.bid)) {
			return false;
		}
		if (this.bidSize == null) {
			if (other.bidSize != null) {
				return false;
			}
		}
		else if (!this.bidSize.equals(other.bidSize)) {
			return false;
		}
		if (this.dayHigh == null) {
			if (other.dayHigh != null) {
				return false;
			}
		}
		else if (!this.dayHigh.equals(other.dayHigh)) {
			return false;
		}
		if (this.dayLow == null) {
			if (other.dayLow != null) {
				return false;
			}
		}
		else if (!this.dayLow.equals(other.dayLow)) {
			return false;
		}
		if (this.instrument == null) {
			if (other.instrument != null) {
				return false;
			}
		}
		else if (!this.instrument.equals(other.instrument)) {
			return false;
		}
		if (this.lastTradeDateStr == null) {
			if (other.lastTradeDateStr != null) {
				return false;
			}
		}
		else if (!this.lastTradeDateStr.equals(other.lastTradeDateStr)) {
			return false;
		}
		if (this.lastTradeSize == null) {
			if (other.lastTradeSize != null) {
				return false;
			}
		}
		else if (!this.lastTradeSize.equals(other.lastTradeSize)) {
			return false;
		}
		if (this.lastTradeTime == null) {
			if (other.lastTradeTime != null) {
				return false;
			}
		}
		else if (!this.lastTradeTime.equals(other.lastTradeTime)) {
			return false;
		}
		if (this.lastTradeTimeStr == null) {
			if (other.lastTradeTimeStr != null) {
				return false;
			}
		}
		else if (!this.lastTradeTimeStr.equals(other.lastTradeTimeStr)) {
			return false;
		}
		if (this.open == null) {
			if (other.open != null) {
				return false;
			}
		}
		else if (!this.open.equals(other.open)) {
			return false;
		}
		if (this.previousClose == null) {
			if (other.previousClose != null) {
				return false;
			}
		}
		else if (!this.previousClose.equals(other.previousClose)) {
			return false;
		}
		if (this.price == null) {
			if (other.price != null) {
				return false;
			}
		}
		else if (!this.price.equals(other.price)) {
			return false;
		}
		if (this.priceAvg200 == null) {
			if (other.priceAvg200 != null) {
				return false;
			}
		}
		else if (!this.priceAvg200.equals(other.priceAvg200)) {
			return false;
		}
		if (this.priceAvg50 == null) {
			if (other.priceAvg50 != null) {
				return false;
			}
		}
		else if (!this.priceAvg50.equals(other.priceAvg50)) {
			return false;
		}
		if (this.timeZone == null) {
			if (other.timeZone != null) {
				return false;
			}
		}
		else if (!this.timeZone.equals(other.timeZone)) {
			return false;
		}
		if (this.volume == null) {
			if (other.volume != null) {
				return false;
			}
		}
		else if (!this.volume.equals(other.volume)) {
			return false;
		}
		if (this.yearHigh == null) {
			if (other.yearHigh != null) {
				return false;
			}
		}
		else if (!this.yearHigh.equals(other.yearHigh)) {
			return false;
		}
		if (this.yearLow == null) {
			if (other.yearLow != null) {
				return false;
			}
		}
		else if (!this.yearLow.equals(other.yearLow)) {
			return false;
		}
		return true;
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
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.ask == null) ? 0 : this.ask.hashCode());
		result = (prime * result) + ((this.askSize == null) ? 0 : this.askSize.hashCode());
		result = (prime * result) + ((this.avgVolume == null) ? 0 : this.avgVolume.hashCode());
		result = (prime * result) + ((this.bid == null) ? 0 : this.bid.hashCode());
		result = (prime * result) + ((this.bidSize == null) ? 0 : this.bidSize.hashCode());
		result = (prime * result) + ((this.dayHigh == null) ? 0 : this.dayHigh.hashCode());
		result = (prime * result) + ((this.dayLow == null) ? 0 : this.dayLow.hashCode());
		result = (prime * result) + ((this.instrument == null) ? 0 : this.instrument.hashCode());
		result = (prime * result)
		        + ((this.lastTradeDateStr == null) ? 0 : this.lastTradeDateStr.hashCode());
		result = (prime * result)
		        + ((this.lastTradeSize == null) ? 0 : this.lastTradeSize.hashCode());
		result = (prime * result)
		        + ((this.lastTradeTime == null) ? 0 : this.lastTradeTime.hashCode());
		result = (prime * result)
		        + ((this.lastTradeTimeStr == null) ? 0 : this.lastTradeTimeStr.hashCode());
		result = (prime * result) + ((this.open == null) ? 0 : this.open.hashCode());
		result = (prime * result)
		        + ((this.previousClose == null) ? 0 : this.previousClose.hashCode());
		result = (prime * result) + ((this.price == null) ? 0 : this.price.hashCode());
		result = (prime * result) + ((this.priceAvg200 == null) ? 0 : this.priceAvg200.hashCode());
		result = (prime * result) + ((this.priceAvg50 == null) ? 0 : this.priceAvg50.hashCode());
		result = (prime * result) + ((this.timeZone == null) ? 0 : this.timeZone.hashCode());
		result = (prime * result) + ((this.volume == null) ? 0 : this.volume.hashCode());
		result = (prime * result) + ((this.yearHigh == null) ? 0 : this.yearHigh.hashCode());
		result = (prime * result) + ((this.yearLow == null) ? 0 : this.yearLow.hashCode());
		return result;
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
