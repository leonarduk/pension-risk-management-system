package com.leonarduk.finance.utils;

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.datatransformation.correction.BadDateRemover;
import com.leonarduk.finance.stockfeed.datatransformation.correction.BadScalingCorrector;
import com.leonarduk.finance.stockfeed.datatransformation.correction.NullValueRemover;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.LinearInterpolator;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.DoubleNumFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.ZoneId;

public enum TimeseriesUtils {
    ;

    public static int cleanUpSeries(Optional<StockV1> liveData) throws IOException {
        if (liveData.isPresent()) {
            List<Bar> history = liveData.get().getHistory();
            int original = history.size();
            List<Bar> clean = new BadScalingCorrector().clean(new BadDateRemover().clean(new NullValueRemover().clean(history)));

            // TODO scale to/from USD to GBP or GBX
            liveData.get().setHistory(clean);

            int fixed = clean.size();
            return original - fixed;
        }
        return 0;
    }

    public static boolean containsDatePoints(List<Bar> cachedHistory, LocalDate... dates) {
        return TimeseriesUtils.getMissingDataPoints(cachedHistory, dates).isEmpty();
    }

    public static List<LocalDate> getMissingDataPointsForDateRange(List<Bar> cachedHistory, LocalDate fromdate, LocalDate toDate) {
        final LocalDate[] range = new DateRange(fromdate, toDate).toList().toArray(new LocalDate[0]);
        return TimeseriesUtils.getMissingDataPoints(cachedHistory, range);
    }

    public static List<LocalDate> getMissingDataPoints(List<Bar> cachedHistory, LocalDate... dates) {
        final Set<LocalDate> daysWithData = cachedHistory.stream().map(quote -> quote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());
        return Arrays.stream(dates).filter(date -> !daysWithData.contains(date)).collect(Collectors.toList());
    }

    public static Comparator<? super Bar> getComparator() {
        return Comparator.comparing(Bar::getEndTime);
    }

    public static Bar getMostRecentQuote(List<Bar> history) {
        return history.get(history.size() - 1);
    }

    public static Bar getOldestQuote(List<Bar> history) {
        return history.get(0);
    }

    public static BarSeries getTimeSeries(StockV1 stock, int i, final boolean addLatestQuoteToTheSeries) throws IOException {
        return getTimeSeries(stock, LocalDate.now().minusYears(i), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    public static BarSeries getTimeSeries(StockV1 stock, LocalDate fromDate, LocalDate toDate, final boolean addLatestQuoteToTheSeries)
            throws IOException {
        List<Bar> history = stock.getHistory();
        if ((null == history) || history.isEmpty()) {
            Optional<StockV1> optional = new IntelligentStockFeed(new FileBasedDataStore("db")).get(stock.getInstrument(), fromDate, toDate,
                    addLatestQuoteToTheSeries);
            if (optional.isPresent()) {
                history = optional.get().getHistory();
            } else {
                return null;
            }
        }

        sortQuoteList(history);
        Iterator<Bar> series = history.iterator();

        List<Bar> ticks = new LinkedList<>();
        while (series.hasNext()) {
            try {
                ticks.add(series.next());
            } catch (NullPointerException e) {
                System.err.println(e);
                return null;
            }
        }
        return new LinearInterpolator().interpolate(
                new BaseBarSeriesBuilder().withName(stock.getName()).withNumFactory(DoubleNumFactory.getInstance()).withBars(ticks).build());
    }

    public static Bar createSyntheticQuote(Bar currentQuote, LocalDate currentDate,
                                           BigDecimal newClosePriceRaw, BigDecimal newOpenPriceRaw, String comment)
            throws IOException {
        BigDecimal newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
        BigDecimal newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);
        return new ExtendedHistoricalQuote(currentQuote.getDateName(), currentDate, newOpenPrice,
                newClosePrice.min(newOpenPrice), newClosePrice.max(newOpenPrice), newClosePrice, newClosePrice,
                0L, comment);
    }

    public static Bar createSyntheticBar(LocalDate currentDate, Double newClosePriceRaw,
                                         Double newOpenPriceRaw, final String comment) {

        double newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
        double newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);
        return new ExtendedHistoricalQuote("", currentDate, BigDecimal.valueOf(newOpenPrice),
                BigDecimal.valueOf(Double.min(newClosePrice, newOpenPrice)), BigDecimal.valueOf(newClosePrice),
                BigDecimal.valueOf(Double.max(newClosePrice, newOpenPrice)), BigDecimal.valueOf(newClosePrice),
                0L, comment);
    }

    public static Optional<StockV1> interpolateAndSortSeries(LocalDate fromLocalDate, LocalDate toLocalDate,
                                                             boolean interpolate, Optional<StockV1> liveData) throws IOException {
        List<Bar> history = liveData.get().getHistory();
        if (interpolate) {
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();

            final List<Bar> series = history; //flatLineInterpolator.extendToFromDate(history, fromLocalDate);
            history = linearInterpolator.interpolate(flatLineInterpolator
                    .extendToToDate(series, toLocalDate));
        }
        List<Bar> subSeries = history.stream()
                .filter(q -> (q.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(fromLocalDate)
                        && q.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(toLocalDate))
                        || q.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(fromLocalDate)
                        || q.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(toLocalDate))
                .collect(Collectors.toList());
        sortQuoteList(subSeries);
        liveData.get().setHistory(subSeries);
        return liveData;
    }

    public static StringBuilder seriesToCsv(List<Bar> series) {
        StringBuilder sb = new StringBuilder("date,open,high,low,close,volume,comment\n");
        // TODO add comment field if necessary- look at how HTML tools does it
        for (Bar historicalQuote : series) {
            sb.append(historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString());
            StringUtils.addValue(sb, historicalQuote.getOpenPrice());
            StringUtils.addValue(sb, historicalQuote.getHighPrice());
            StringUtils.addValue(sb, historicalQuote.getLowPrice());
            StringUtils.addValue(sb, historicalQuote.getClosePrice());
            StringUtils.addValue(sb, historicalQuote.getVolume());
            if (historicalQuote instanceof final Commentable commentable) {
                sb.append(",").append(commentable.getComment());
            }
            sb.append("\n");
        }
        return sb;
    }

    public static List<Bar> sortQuoteList(List<Bar> history) {
        history.sort(getComparator());
        return history;
    }

    public static class DateRange implements Iterable<LocalDate> {

        private final LocalDate startDate;
        private final LocalDate endDate;

        public DateRange(final LocalDate startDate, final LocalDate endDate) {
            //check that range is valid (null, start < end)
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public Iterator<LocalDate> iterator() {
            return this.stream().iterator();
        }

        public Stream<LocalDate> stream() {
            return Stream.iterate(this.startDate, d -> d.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(this.startDate, this.endDate) + 1);
        }

        public List<LocalDate> toList() { //could also be built from the stream() method
            final List<LocalDate> dates = new ArrayList<>();
            for (LocalDate d = this.startDate; !d.isAfter(this.endDate); d = d.plusDays(1)) {
                if (!DateUtils.isWeekend().test(d)) {
                    dates.add(d);
                }
            }
            return dates;
        }
    }
}
