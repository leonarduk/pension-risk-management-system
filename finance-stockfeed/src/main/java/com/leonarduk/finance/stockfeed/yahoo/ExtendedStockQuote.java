package com.leonarduk.finance.stockfeed.yahoo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

import com.leonarduk.finance.stockfeed.Instrument;

import yahoofinance.quotes.stock.StockQuote;

public class ExtendedStockQuote extends StockQuote {
	public ExtendedStockQuote(BigDecimal ask, Long askSize, Long avgVolume, BigDecimal bid, Long bidSize,
			BigDecimal dayHigh, BigDecimal dayLow, Instrument instrument, String lastTradeDateStr, Long lastTradeSize,
			Calendar lastTradeTime, String lastTradeTimeStr, BigDecimal open, BigDecimal previousClose,
			BigDecimal price, BigDecimal priceAvg200, BigDecimal priceAvg50, TimeZone timeZone, Long volume,
			BigDecimal yearHigh, BigDecimal yearLow) {
		super(instrument.getCode());
		setAsk(ask);
		setAskSize(askSize);
		setAvgVolume(avgVolume);
		setBid(bid);
		setBidSize(bidSize);
		setDayHigh(dayHigh);
		setDayLow(dayLow);
		setLastTradeDateStr(lastTradeDateStr);
		setLastTradeSize(lastTradeSize);
		setLastTradeTime(lastTradeTime);
		setLastTradeTimeStr(lastTradeTimeStr);
		setOpen(open);
		setPreviousClose(previousClose);
		setPrice(price);
		setPriceAvg200(priceAvg200);
		setPriceAvg50(priceAvg50);
		setTimeZone(timeZone);
		setVolume(volume);
		setYearHigh(yearHigh);
		setYearLow(yearLow);
	}

	public ExtendedStockQuote(StockQuote quote) {
		super(quote.getSymbol());
		setAsk(quote.getAsk());
		setAskSize(quote.getAskSize());
		setAvgVolume(quote.getAvgVolume());
		setBid(quote.getBid());
		setBidSize(quote.getBidSize());
		setDayHigh(quote.getDayHigh());
		setDayLow(quote.getDayLow());
		setLastTradeDateStr(quote.getLastTradeDateStr());
		setLastTradeSize(quote.getLastTradeSize());
		setLastTradeTime(quote.getLastTradeTime());
		setLastTradeTimeStr(quote.getLastTradeTimeStr());
		setOpen(quote.getOpen());
		setPreviousClose(quote.getPreviousClose());
		setPrice(quote.getPrice());
		setPriceAvg200(quote.getPriceAvg200());
		setPriceAvg50(quote.getPriceAvg50());
		setTimeZone(quote.getTimeZone());
		setVolume(quote.getVolume());
		setYearHigh(quote.getYearHigh());
		setYearLow(quote.getYearLow());
	}

	public boolean isPopulated() {
		// TODO Auto-generated method stub
		return true;
	}

}