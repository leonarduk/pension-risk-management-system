package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * https://markets.ft.com/data/etfs/tearsheet/historical?s=HMWO:LSE:GBX
 * https://markets.ft.com/data/funds/tearsheet/historical?s=GB00B6ZDFJ91:GBX
 * https://markets.ft.com/data/investment-trust/tearsheet/historical?s=TRG:LSE
 * https://markets.ft.com/data/equities/tearsheet/summary?s=TSCO:LSE
 */
public class FTFeed  extends AbstractStockFeed {
    public static final Logger log = LoggerFactory.getLogger(FTFeed.class.getName());

    @Override
    public Optional<StockV1> get(Instrument instrument, int years, boolean addLatestQuoteToTheSeries) throws IOException {
        FTInstrument ftInstrument = new FTInstrument(instrument);
        log.info("Fetch from " + ftInstrument.getFTUrl());
        return Optional.empty();
    }

    @Override
    public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException {
        FTInstrument ftInstrument = new FTInstrument(instrument);
        log.info("Fetch from " + ftInstrument.getFTUrl());
        return Optional.empty();
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
