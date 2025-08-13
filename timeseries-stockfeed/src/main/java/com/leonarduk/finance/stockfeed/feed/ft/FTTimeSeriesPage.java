package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.web.BaseSeleniumPage;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FTTimeSeriesPage extends BaseSeleniumPage {
    public static final Logger log = LoggerFactory.getLogger(FTTimeSeriesPage.class.getName());

    public FTTimeSeriesPage(WebDriver webDriver, String expectedUrl) {
        super(webDriver, expectedUrl);
    }

    @Override
    protected void load() {
        log.info("Load {}", this.getExpectedUrl());
        this.getWebDriver().get(getExpectedUrl());

    }

    public List<Bar> getTimeseries(Instrument instrument, LocalDate fromDate, LocalDate toDate) {

        // e.g. 2021-04-01
        DateTimeFormatter numericDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // e.g. Fri, Aug 20, 2021
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");

        String fromDateString = fromDate.format(numericDateFormatter);
        String toDateString = toDate.format(numericDateFormatter);

        String url = String.format("%s%sstartDate=%s&endDate=%s", getExpectedUrl(),
                getExpectedUrl().contains("?") ? "&" : "?", fromDateString, toDateString);
        log.info("Load {}", url);
        this.getWebDriver().get(url);

        WebElement table = this.getWebDriver().findElement(By.className("mod-tearsheet-historical-prices__results"));
        WebElement body = table.findElement(By.tagName("tbody"));
        List<WebElement> rows = body.findElements(By.tagName("tr"));

        return rows.stream()
                .map(row -> {
                    Iterator<WebElement> fieldsIter = row.findElements(By.tagName("td")).iterator();
                    String dateString = fieldsIter.next().findElements(By.tagName("span")).get(1).getAttribute("innerHTML");
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    double open = parseDouble(fieldsIter.next().getText());
                    double high = parseDouble(fieldsIter.next().getText());
                    double low = parseDouble(fieldsIter.next().getText());
                    double close = parseDouble(fieldsIter.next().getText());
                    WebElement webElement = fieldsIter.next();
                    long volume = parseLong(webElement.findElements(By.tagName("span")).get(1).getAttribute("innerHTML"));
                    return new ExtendedHistoricalQuote(instrument, date,
                            open, low, high, close, close,
                            volume, "FTFeed");
                })
                .filter(bar -> {
                    LocalDate date = bar.getEndTime().toLocalDate();
                    return !(date.isBefore(fromDate) || date.isAfter(toDate));
                })
                .collect(Collectors.toList());
    }

    private Double parseDouble(String text) {
        try {
            if (text.contains("k")) // 10.08k
            {
                return 1000 * parseDouble(text.replaceAll("k", ""));
            }
            return Double.valueOf(StringUtils.defaultIfBlank(text, "0.0").replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Long parseLong(String text) {
        return parseDouble(text).longValue();
    }

}
