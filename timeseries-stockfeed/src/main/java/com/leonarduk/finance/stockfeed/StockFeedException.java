package com.leonarduk.finance.stockfeed;

/**
 * Exception thrown when no stock data can be retrieved from any source.
 */
public class StockFeedException extends RuntimeException {
    public StockFeedException(String message) {
        super(message);
    }

    public StockFeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
