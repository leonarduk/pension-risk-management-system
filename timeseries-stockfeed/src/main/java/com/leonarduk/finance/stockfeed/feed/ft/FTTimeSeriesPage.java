package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FTTimeSeriesPage {
    public static final Logger log = LoggerFactory.getLogger(FTTimeSeriesPage.class.getName());

    private final WebDriver webDriver;
    private final String expectedUrl;

    public FTTimeSeriesPage(WebDriver webDriver, String expectedUrl) {
        this.webDriver = webDriver;
        this.expectedUrl = expectedUrl;
    }

    public Optional<List<Bar>> getTimeseries(Instrument instrument, LocalDate fromDate, LocalDate toDate) {

        // e.g. 2021-04-01
        DateTimeFormatter numericDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // e.g. Fri, Aug 20, 2021
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");

        String fromDateString = fromDate.format(numericDateFormatter);
        String toDateString = toDate.format(numericDateFormatter);

        String url = String.format("%s%sstartDate=%s&endDate=%s", expectedUrl,
                expectedUrl.contains("?") ? "&" : "?", fromDateString, toDateString);
        log.info("Load {}", url);
        this.webDriver.get(url);

        try {
            WebElement table = new WebDriverWait(webDriver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(".mod-tearsheet-historical-prices__results table")));
            WebElement body = table.findElement(By.tagName("tbody"));
            List<WebElement> rows = body.findElements(By.tagName("tr"));

            List<Bar> bars = rows.stream()
                    .map(row -> {
                        Iterator<WebElement> fieldsIter = row.findElements(By.tagName("td")).iterator();
                        String dateString = fieldsIter.next().findElements(By.tagName("span")).get(1)
                                .getAttribute("innerHTML");
                        LocalDate date = LocalDate.parse(dateString, formatter);
                        double open = parseDouble(fieldsIter.next().getText());
                        double high = parseDouble(fieldsIter.next().getText());
                        double low = parseDouble(fieldsIter.next().getText());
                        double close = parseDouble(fieldsIter.next().getText());
                        WebElement webElement = fieldsIter.next();
                        long volume = parseLong(webElement.findElements(By.tagName("span")).get(1)
                                .getAttribute("innerHTML"));
                        return new ExtendedHistoricalQuote(instrument, date,
                                open, low, high, close, close,
                                volume, "FTFeed");
                    })
                    .filter(bar -> {
                        LocalDate date = bar.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate();
                        return !(date.isBefore(fromDate) || date.isAfter(toDate));
                    })
                    .collect(Collectors.toList());

            return Optional.of(bars);
        } catch (TimeoutException | NoSuchElementException e) {
            log.warn("Unable to parse FT HTML table for {}", instrument, e);
            return getTimeseriesFromCsv(instrument, fromDate, toDate);
        }
    }

    private Optional<List<Bar>> getTimeseriesFromCsv(Instrument instrument, LocalDate fromDate, LocalDate toDate) {
        try {
            DateTimeFormatter numericDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fromDateString = fromDate.format(numericDateFormatter);
            String toDateString = toDate.format(numericDateFormatter);
            String url = String.format("%s%sstartDate=%s&endDate=%s&format=csv", expectedUrl,
                    expectedUrl.contains("?") ? "&" : "?", fromDateString, toDateString);
            log.info("CSV fallback {}", url);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                List<Bar> bars = reader.lines()
                        .skip(1)
                        .map(line -> parseCsvLine(instrument, line))
                        .filter(Objects::nonNull)
                        .filter(bar -> {
                            LocalDate date = bar.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate();
                            return !(date.isBefore(fromDate) || date.isAfter(toDate));
                        })
                        .collect(Collectors.toList());
                return Optional.of(bars);
            }
        } catch (Exception ex) {
            log.warn("CSV fallback failed for {}", instrument, ex);
            return Optional.empty();
        }
    }

    private Bar parseCsvLine(Instrument instrument, String line) {
        String[] tokens = line.split(",");
        if (tokens.length < 6) {
            return null;
        }
        LocalDate date = LocalDate.parse(tokens[0], DateTimeFormatter.ISO_LOCAL_DATE);
        double open = parseDouble(tokens[1]);
        double high = parseDouble(tokens[2]);
        double low = parseDouble(tokens[3]);
        double close = parseDouble(tokens[4]);
        long volume = parseLong(tokens[5]);
        return new ExtendedHistoricalQuote(instrument, date, open, low, high, close, close, volume, "FTFeed");
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
