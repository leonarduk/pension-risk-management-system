module timeseries.stockfeed {
    exports com.leonarduk.finance.utils;
    exports com.leonarduk.finance.stockfeed;
    exports com.leonarduk.finance.stockfeed.feed.alphavantage;
    exports com.leonarduk.finance.stockfeed.file;
    exports com.leonarduk.finance.stockfeed.datatransformation.correction;
    exports com.leonarduk.finance.stockfeed.feed;
    exports com.leonarduk.finance.stockfeed.feed.yahoofinance;
    exports com.leonarduk.finance.scraper;
    requires org.patriques.alphavantage4j;
    requires com.google.common;
    requires htmlunit.driver;
    requires http.request;
    requires influxdb.client.core;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires org.htmlunit;
    requires org.seleniumhq.selenium.api;
    requires org.slf4j;
    requires ta4j.core;
    requires YahooFinanceAPI;
    requires com.fasterxml.jackson.annotation;
}