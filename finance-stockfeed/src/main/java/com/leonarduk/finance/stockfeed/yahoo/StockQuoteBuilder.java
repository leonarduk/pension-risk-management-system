package com.leonarduk.finance.stockfeed.yahoo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

import com.leonarduk.finance.stockfeed.Instrument;

import yahoofinance.quotes.stock.StockQuote;

public class StockQuoteBuilder {
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
			return new ExtendedStockQuote(this.ask, this.askSize, this.avgVolume,
			        this.bid, this.bidSize, this.dayHigh, this.dayLow,
			        this.instrument, this.lastTradeDateStr, this.lastTradeSize,
			        this.lastTradeTime, this.lastTradeTimeStr, this.open,
			        this.previousClose, this.price, this.priceAvg200,
			        this.priceAvg50, this.timeZone, this.volume, this.yearHigh,
			        this.yearLow);
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

		public StockQuoteBuilder setLastTradeDateStr(
		        final String lastTradeDateStr) {
			this.lastTradeDateStr = lastTradeDateStr;
			return this;
		}

		public StockQuoteBuilder setLastTradeSize(final Long lastTradeSize) {
			this.lastTradeSize = lastTradeSize;
			return this;
		}

		public StockQuoteBuilder setLastTradeTime(
		        final Calendar lastTradeTime) {
			this.lastTradeTime = lastTradeTime;
			return this;
		}

		public StockQuoteBuilder setLastTradeTimeStr(
		        final String lastTradeTimeStr) {
			this.lastTradeTimeStr = lastTradeTimeStr;
			return this;
		}

		public StockQuoteBuilder setOpen(final BigDecimal open) {
			this.open = open;
			return this;
		}

		public StockQuoteBuilder setPreviousClose(
		        final BigDecimal previousClose) {
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