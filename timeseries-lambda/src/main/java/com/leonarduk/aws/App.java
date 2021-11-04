//package com.leonarduk.aws;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.google.common.collect.Lists;
//import com.leonarduk.finance.stockfeed.Instrument;
//import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
//import com.leonarduk.finance.stockfeed.StockFeed;
//import com.leonarduk.finance.stockfeed.feed.Commentable;
//import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
//import com.leonarduk.finance.utils.DataField;
//import com.leonarduk.finance.utils.HtmlTools;
//import org.apache.commons.lang3.StringUtils;
//import org.ta4j.core.Bar;
//import software.amazon.awssdk.services.s3.S3Client;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Lambda function entry point. You can change to use other pojo type or implement
// * a different RequestHandler.
// *
// * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
// */
//public class App implements RequestHandler<Object, Object> {
//    private final S3Client s3Client;
//    private final StockFeed stockFeed;
//
//    public App() {
//        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
//        // It is initialized when the class is loaded.
//        s3Client = DependencyFactory.s3Client();
//        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
//        stockFeed = DependencyFactory.stockFeed();
//    }
//
//    public static void main(String[] args) {
//       System.out.println( new App().handleRequest(null, null));
//    }
//    @Override
//    public Object handleRequest(final Object input, final Context context) {
//        // TODO: invoking the api call using s3Client.
//        try {
//
//            final int years = 10;
//            final String fromDate = "";
//            final String toDate = "";
//            final boolean interpolate = false;
//            final boolean cleanData = false;
//            final Instrument instrument = Instrument.fromString("PHGP.L");
//            String[] fields = {};
//            boolean addLatestQuoteToTheSeries = false;
//            return generateResults(years, fromDate, toDate, interpolate, cleanData, instrument, fields, addLatestQuoteToTheSeries);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private String generateResults(final int years, final String fromDate, final String toDate,
//                                   final boolean interpolate, final boolean cleanData,
//                                   final Instrument instrument, String[] fields,
//                                   boolean addLatestQuoteToTheSeries)
//            throws IOException {
//        final StringBuilder sbBody = new StringBuilder();
//        final List<List<DataField>> records = Lists.newArrayList();
//
//        final List<Bar> historyData;
//        LocalDate toLocalDate;
//        final LocalDate fromLocalDate;
//
//        if (!StringUtils.isEmpty(fromDate)) {
//            fromLocalDate = LocalDate.parse(fromDate);
//            if (StringUtils.isEmpty(toDate)) {
//                toLocalDate = LocalDate.now();
//            } else {
//                toLocalDate = LocalDate.parse(toDate);
//            }
//
//        } else {
//            toLocalDate = LocalDate.now();
//            fromLocalDate = LocalDate.now().plusYears(-1 * years);
//        }
//
//        historyData = this.getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, addLatestQuoteToTheSeries);
//
//        for (final Bar historicalQuote : historyData) {
//            final ArrayList<DataField> record = Lists.newArrayList();
//            records.add(record);
//            record.add(new DataField("Date", historicalQuote.getEndTime().toLocalDate().toString()));
//            record.add(new DataField("Open", historicalQuote.getOpenPrice()));
//            record.add(new DataField("High", historicalQuote.getMaxPrice()));
//            record.add(new DataField("Low", historicalQuote.getMinPrice()));
//            record.add(new DataField("Close", historicalQuote.getClosePrice()));
//            record.add(new DataField("Volume", historicalQuote.getVolume()));
//
//            if (historicalQuote instanceof Commentable) {
//                Commentable commentable = (Commentable) historicalQuote;
//                record.add(new DataField("Comment", commentable.getComment()));
//
//            }
//        }
//
//        HtmlTools.printTable(sbBody, records);
//        return HtmlTools.createHtmlText(null, sbBody).toString();
//    }
//
//    private List<Bar> getHistoryData(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate,
//                                     boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException {
//        final Optional<StockV1> stock = this.stockFeed.get(instrument, fromLocalDate, toLocalDate, interpolate,
//                cleanData, addLatestQuoteToTheSeries);
//        if (stock.isPresent()) {
//            return stock.get().getHistory();
//        }
//        return Lists.newArrayList();
//    }
//
//}