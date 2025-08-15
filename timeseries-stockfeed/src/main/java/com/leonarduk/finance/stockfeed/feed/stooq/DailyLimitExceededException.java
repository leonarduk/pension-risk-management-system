package com.leonarduk.finance.stockfeed.feed.stooq;

import java.io.IOException;

public class DailyLimitExceededException extends IOException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}
