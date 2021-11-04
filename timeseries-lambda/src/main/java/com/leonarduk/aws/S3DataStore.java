//package com.leonarduk.aws;
//
//import com.leonarduk.finance.stockfeed.DataStore;
//import com.leonarduk.finance.stockfeed.Instrument;
//import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.Optional;
//
//public class S3DataStore implements DataStore {
//    @Override
//    public void storeSeries(StockV1 stock) throws IOException {
//
//    }
//
//    @Override
//    public boolean isAvailable() {
//        return false;
//    }
//
//    @Override
//    public Optional<StockV1> get(Instrument instrument, int years, boolean addLatest) throws IOException {
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatest) throws IOException {
//        return Optional.empty();
//    }
//
//    @Override
//    public boolean contains(StockV1 stock) throws IOException {
//        return false;
//    }
//}
