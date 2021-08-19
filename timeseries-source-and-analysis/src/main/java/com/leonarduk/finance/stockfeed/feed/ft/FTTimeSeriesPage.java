package com.leonarduk.finance.stockfeed.feed.ft;
import com.leonarduk.web.BaseSeleniumPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTTimeSeriesPage extends BaseSeleniumPage{
    public static final Logger log = LoggerFactory.getLogger(FTTimeSeriesPage.class.getName());

    public FTTimeSeriesPage(WebDriver webDriver, String expectedUrl) {
        super(webDriver, expectedUrl);
        log.info("Load " + this.getExpectedUrl());
        this.get();
    }

    @Override
    protected void load() {
    }
}
