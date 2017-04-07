package com.leonarduk.stockmarketview.stockfeed;

import java.io.IOException;

import yahoofinance.Stock;

public interface StockFeed {
	public enum EXCHANGE {
		London
	}

	Stock get(EXCHANGE exchange, String ticker) throws IOException;

}
