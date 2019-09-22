package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

public class FxInstrument extends Instrument {

	public FxInstrument(Source source, String currencyOne, String currencyTwo) {
		super(currencyTwo + "/" + currencyTwo, AssetType.FX, AssetType.FX, Source.alphavantage,
				currencyTwo + currencyTwo, currencyTwo + currencyTwo, Exchange.NA, currencyTwo, currencyTwo,
				currencyTwo);
		this.currencyOne = currencyOne;
		this.currencyTwo = currencyTwo;
	}

	private String currencyOne;
	private String currencyTwo;

	public String getCurrencyOne() {
		return currencyOne;
	}

	public String getCurrencyTwo() {
		return currencyTwo;
	}
}
