package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

public final class FxInstrument extends Instrument {

    public FxInstrument(Source source, String currencyOne, String currencyTwo) {
        super(currencyTwo + "/" + currencyTwo, AssetType.FX, AssetType.FX, Source.ALPHAVANTAGE,
                currencyOne + currencyTwo, currencyOne + currencyTwo, Exchange.NA, currencyOne, currencyTwo,
                currencyTwo);
        this.currencyOne = currencyOne;
        this.currencyTwo = currencyTwo;
    }

    private final String currencyOne;
    private final String currencyTwo;

    public String getCurrencyOne() {
        return currencyOne;
    }

    public String getCurrencyTwo() {
        return currencyTwo;
    }
}
