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

public class TimeseriesUtils {
    public static int cleanUpSeries(final Optional<StockV1> liveData) throws IOException {
        if (liveData.isPresent()) {
            final List<Bar> history = liveData.get().getHistory();
            final int original = history.size();
            final List<Bar> clean = new BadScalingCorrector().clean(new BadDateRemover().clean(new NullValueRemover().clean(history)));

            // TODO scale to/from USD to GBP or GBX
            liveData.get().setHistory(clean);

            final int fixed = clean.size();
            return original - fixed;
        }
        return 0;
    }

    public static boolean containsDatePoints(final List<Bar> cachedHistory, final LocalDate... dates) {
        return getMissingDataPoints(cachedHistory, dates).isEmpty();
    }

    public static List<LocalDate> getMissingDataPointsForDateRange(final List<Bar> cachedHistory, final LocalDate fromdate, final LocalDate toDate) {
        LocalDate[] range = new DateRange(fromdate, toDate).toList().toArray(new LocalDate[0]);
        return getMissingDataPoints(cachedHistory, range);
    }

    public static List<LocalDate> getMissingDataPoints(final List<Bar> cachedHistory, final LocalDate... dates) {
        Set<LocalDate> daysWithData = cachedHistory.stream()
                .map(quote -> quote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toCollection(HashSet::new));
        return Arrays.stream(dates).filter(date -> !daysWithData.contains(date)).collect(Collectors.toList());
    }

    public static Comparator<? super Bar> getComparator() {
        return Comparator.comparing(Bar::getEndTime);
    }

    public static Bar getMostRecentQuote(final List<Bar> history) {
        return history.get(history.size() - 1);
    }

    public static Bar getOldestQuote(final List<Bar> history) {
        return history.get(0);
    }

    public static BarSeries getTimeSeries(final StockV1 stock, final int i, boolean addLatestQuoteToTheSeries) throws IOException {
        return TimeseriesUtils.getTimeSeries(stock, LocalDate.now().minusYears(i), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    public static BarSeries getTimeSeries(final StockV1 stock, final LocalDate fromDate, final LocalDate toDate, boolean addLatestQuoteToTheSeries)
            throws IOException {
        List<Bar> history = stock.getHistory();
        if ((null == history) || history.isEmpty()) {
            final Optional<StockV1> optional = new IntelligentStockFeed(new FileBasedDataStore("db")).get(stock.getInstrument(), fromDate, toDate,
                    addLatestQuoteToTheSeries);
            if (optional.isPresent()) {
                history = optional.get().getHistory();
            } else {
                return null;
            }
        }

        TimeseriesUtils.sortQuoteList(history);
        final Iterator<Bar> series = history.iterator();

        final List<Bar> ticks = new LinkedList<>();
        while (series.hasNext()) {
            try {
                ticks.add(series.next());
            } catch (final NullPointerException e) {
                System.err.println(e);
                return null;
            }
        }
        return new LinearInterpolator().interpolate(
                new BaseBarSeriesBuilder().withName(stock.getName()).withNumFactory(DoubleNumFactory.getInstance()).withBars(ticks).build());
    }

    public static Bar createSyntheticQuote(final Bar currentQuote, final LocalDate currentDate,
                                           final BigDecimal newClosePriceRaw, final BigDecimal newOpenPriceRaw, final String comment)
            throws IOException {
        final BigDecimal newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
        final BigDecimal newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);
        return new ExtendedHistoricalQuote(currentQuote.getDateName(), currentDate, newOpenPrice,
                newClosePrice.min(newOpenPrice), newClosePrice.max(newOpenPrice), newClosePrice, newClosePrice,
                DoubleNum.valueOf(0), comment);
    }

    public static Bar createSyntheticBar(final LocalDate currentDate, final Double newClosePriceRaw,
                                         final Double newOpenPriceRaw, String comment) {

        final double newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
        final double newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);
        return new ExtendedHistoricalQuote("", currentDate, BigDecimal.valueOf(newOpenPrice),
                BigDecimal.valueOf(Double.min(newClosePrice, newOpenPrice)), BigDecimal.valueOf(newClosePrice),
                BigDecimal.valueOf(Double.max(newClosePrice, newOpenPrice)), BigDecimal.valueOf(newClosePrice),
                DoubleNum.valueOf(0), comment);
    }

    public static Optional<StockV1> interpolateAndSortSeries(final LocalDate fromLocalDate, final LocalDate toLocalDate,
                                                             final boolean interpolate, final Optional<StockV1> liveData) throws IOException {
        List<Bar> history = liveData.get().getHistory();
        if (interpolate) {
            final LinearInterpolator linearInterpolator = new LinearInterpolator();
            final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();

            List<Bar> series = history; //flatLineInterpolator.extendToFromDate(history, fromLocalDate);
            history = linearInterpolator.interpolate(flatLineInterpolator
                    .extendToToDate(series, toLocalDate));
        }
        final List<Bar> subSeries = history.stream()
                .filter(q -> {
                    LocalDate endDate = q.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate();
                    return (endDate.isAfter(fromLocalDate)
                            && endDate.isBefore(toLocalDate))
                            || endDate.isEqual(fromLocalDate)
                            || endDate.isEqual(toLocalDate);
                })
                .collect(Collectors.toList());
        TimeseriesUtils.sortQuoteList(subSeries);
        liveData.get().setHistory(subSeries);
        return liveData;
    }

    public static StringBuilder seriesToCsv(final List<Bar> series) {
        final StringBuilder sb = new StringBuilder("date,open,high,low,close,volume,comment\n");
        // TODO add comment field if necessary- look at how HTML tools does it
        for (final Bar historicalQuote : series) {
            sb.append(historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString());
            StringUtils.addValue(sb, historicalQuote.getOpenPrice());
            StringUtils.addValue(sb, historicalQuote.getMaxPrice());
            StringUtils.addValue(sb, historicalQuote.getMinPrice());
            StringUtils.addValue(sb, historicalQuote.getClosePrice());
            StringUtils.addValue(sb, historicalQuote.getVolume());
            if (historicalQuote instanceof Commentable commentable) {
                sb.append(",").append(commentable.getComment());
            }
            sb.append("\n");
        }
        return sb;
    }

    public static List<Bar> sortQuoteList(final List<Bar> history) {
        history.sort(TimeseriesUtils.getComparator());
        return history;
    }

    public static class DateRange implements Iterable<LocalDate> {

        private final LocalDate startDate;
        private final LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            //check that range is valid (null, start < end)
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public Iterator<LocalDate> iterator() {
            return stream().iterator();
        }

        public Stream<LocalDate> stream() {
            return Stream.iterate(startDate, d -> d.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1);
        }

        public List<LocalDate> toList() { //could also be built from the stream() method
            List<LocalDate> dates = new ArrayList<>();
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                if (!DateUtils.isWeekend().test(d)) {
                    dates.add(d);
                }
            }
            return dates;
        }
    }
}
