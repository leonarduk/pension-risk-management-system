package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * https://markets.ft.com/data/etfs/tearsheet/historical?s=HMWO:LSE:GBX
 * https://markets.ft.com/data/funds/tearsheet/historical?s=GB00B6ZDFJ91:GBX
 * https://markets.ft.com/data/investment-trust/tearsheet/historical?s=TRG:LSE
 * https://markets.ft.com/data/equities/tearsheet/summary?s=TSCO:LSE
 */
@Slf4j
public class FTFeed extends AbstractStockFeed {

    final WebDriver webDriver;

    public FTFeed() {
        HtmlUnitDriver driver = new HtmlUnitDriver(false);
        WebClient webClient = driver.getWebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webDriver = driver;
    }

    @Override
    public Optional<StockV1> get(Instrument instrument, int years, boolean addLatestQuoteToTheSeries) throws IOException {
        return get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException {
        FTInstrument ftInstrument = new FTInstrument(instrument);
        log.info("Fetch from {} : {}", ftInstrument, instrument);
        return new FTTimeSeriesPage(webDriver, ftInstrument.getFTUrl())
                .getTimeseries(instrument, fromDate, toDate)
                .map(bars -> {
                    try {
                        return new StockV1(instrument, bars);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Source getSource() {
        return Source.FT;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
