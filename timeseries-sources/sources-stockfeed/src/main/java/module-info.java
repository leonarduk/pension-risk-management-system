module sources.stockfeed {
    requires alphavantage4j;
    requires com.google.common;
    requires htmlunit.driver;
    requires http.request;
    requires influxdb.client.core;
    requires java.persistence;
    requires org.apache.commons.lang3;
    requires org.seleniumhq.selenium.api;
    requires org.slf4j;
    requires ta4j.core;
    requires webscraper.core;
    requires YahooFinanceAPI;

    exports com.leonarduk.finance.stockfeed;
    exports com.leonarduk.finance.stockfeed.feed;
    exports com.leonarduk.finance.stockfeed.feed.yahoofinance;
    exports com.leonarduk.finance.stockfeed.file;
    exports com.leonarduk.finance.utils;
}