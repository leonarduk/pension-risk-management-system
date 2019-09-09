package com.leonarduk.finance.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockV1;
import com.leonarduk.finance.stockfeed.interpolation.BadDateRemover;
import com.leonarduk.finance.stockfeed.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.interpolation.LinearInterpolator;
import com.leonarduk.finance.stockfeed.yahoo.ExtendedHistoricalQuote;

import jersey.repackaged.com.google.common.collect.Sets;

public class TimeseriesUtils {
	public static int cleanUpSeries(final Optional<StockV1> liveData) throws IOException {
		final List<ExtendedHistoricalQuote> history = liveData.get().getHistory();
		final int original = history.size();
		final List<ExtendedHistoricalQuote> clean = new BadDateRemover().clean(history);
		liveData.get().setHistory(clean);
		final int fixed = clean.size();
		return original - fixed;
	}

	public static boolean containsDatePoints(final List<ExtendedHistoricalQuote> cachedHistory,
			final LocalDate... dates) {
		final Set<LocalDate> dateSet = Sets.newHashSet(dates);
		return (cachedHistory.stream().filter(quote -> dateSet.contains(quote.getLocaldate()))
				.collect(Collectors.toList()).size() == dateSet.size());
	}

	private static Double ensureIsDouble(final Number bigDecimal) {
		if (bigDecimal == null) {
			return null;
		}
		return bigDecimal.doubleValue();
	}

	public static Comparator<? super ExtendedHistoricalQuote> getComparator() {
		final Comparator<? super ExtendedHistoricalQuote> comparator = (o1, o2) -> {
			return o2.getLocaldate().compareTo(o1.getLocaldate());
		};
		return comparator;
	}

	public static ExtendedHistoricalQuote getMostRecentQuote(final List<ExtendedHistoricalQuote> history) {
		final ExtendedHistoricalQuote firstQuote = history.get(0);
		return firstQuote;
	}

	public static ExtendedHistoricalQuote getOldestQuote(final List<ExtendedHistoricalQuote> history) {
		final ExtendedHistoricalQuote lastQuote = history.get(history.size() - 1);
		return lastQuote;
	}

	public static TimeSeries getTimeSeries(final StockV1 stock, final int i) throws IOException {
		return TimeseriesUtils.getTimeSeries(stock, LocalDate.now().minusYears(i), LocalDate.now());
	}

	public static TimeSeries getTimeSeries(final StockV1 stock, final LocalDate fromDate, final LocalDate toDate)
			throws IOException {
		List<ExtendedHistoricalQuote> history = stock.getHistory();
		if ((null == history) || history.isEmpty()) {
			final Optional<StockV1> optional = new IntelligentStockFeed().get(stock.getInstrument(), fromDate, toDate);
			if (optional.isPresent()) {
				history = optional.get().getHistory();
			} else {
				return null;
			}
		}

		TimeseriesUtils.sortQuoteList(history);
		final Iterator<ExtendedHistoricalQuote> series = history.iterator();

		final List<Bar> ticks = new LinkedList<>();
		while (series.hasNext()) {
			try {
				ticks.add(createBar(series.next()));
			} catch (final NullPointerException e) {
				System.err.println(e);
				return null;
			}
		}
		return new LinearInterpolator().interpolate(new BaseTimeSeries(stock.getName(), ticks));
	}

	public static BaseBar createBar(final ExtendedHistoricalQuote quote) {
		final BigDecimal closeBd = quote.getClose();
		final double open = TimeseriesUtils.ensureIsDouble(TimeseriesUtils.ifNull(quote.getOpen(), closeBd));
		final double high = TimeseriesUtils.ensureIsDouble(TimeseriesUtils.ifNull(quote.getHigh(), closeBd));
		final double low = TimeseriesUtils.ensureIsDouble(TimeseriesUtils.ifNull(quote.getLow(), closeBd));
		final double close = TimeseriesUtils.ensureIsDouble(closeBd);
		final double volume = TimeseriesUtils.ensureIsDouble(TimeseriesUtils.ifNull(quote.getVolume(), 0L));
		ZonedDateTime endTime = quote.getLocaldate().atStartOfDay(ZoneId.of("Europe/London"));
		Function<Number, Num> numFunction = new Function<Number, Num>() {

			@Override
			public Num apply(Number t) {
				return PrecisionNum.valueOf(t.doubleValue());
			}
		};

		return new BaseBar(endTime, open, high, low, close, volume, numFunction);
	}

	public static Iterator<Bar> getTimeSeriesIterator(final TimeSeries series) {
		final Iterator<Bar> iter = new Iterator<Bar>() {
			int index = series.getEndIndex();

			@Override
			public boolean hasNext() {
				return this.index > (series.getBeginIndex() - 1);
			}

			@Override
			public Bar next() {
				return series.getBar(this.index--);
			}
		};
		return iter;
	}

	private static Number ifNull(final Number open, final Number close) {
		if (open == null) {
			return close;
		}
		return open;
	}

	public static Optional<StockV1> interpolateAndSortSeries(final LocalDate fromDate, final LocalDate toDate,
			final boolean interpolate, final Optional<StockV1> liveData) throws IOException {
		List<ExtendedHistoricalQuote> history = liveData.get().getHistory();
		if (interpolate) {
			final LinearInterpolator linearInterpolator = new LinearInterpolator();
			final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();

			history = linearInterpolator.interpolate(flatLineInterpolator
					.extendToToDate(flatLineInterpolator.extendToFromDate(history, fromDate), toDate));
		}
		final List<ExtendedHistoricalQuote> subSeries = history.stream()
				.filter(q -> (q.getLocaldate().isAfter(fromDate) && q.getLocaldate().isBefore(toDate))
						|| q.getLocaldate().isEqual(fromDate) || q.getLocaldate().isEqual(toDate))
				.collect(Collectors.toList());
		TimeseriesUtils.sortQuoteList(subSeries);
		liveData.get().setHistory(subSeries);
		return liveData;
	}

	public static StringBuilder seriesToCsv(final List<ExtendedHistoricalQuote> series) {
		final StringBuilder sb = new StringBuilder("date,open,high,low,close,volume\n");
		for (final ExtendedHistoricalQuote historicalQuote : series) {
			sb.append(historicalQuote.getLocaldate().toString());
			StringUtils.addValue(sb, historicalQuote.getOpen());
			StringUtils.addValue(sb, historicalQuote.getHigh());
			StringUtils.addValue(sb, historicalQuote.getLow());
			StringUtils.addValue(sb, historicalQuote.getClose());
			StringUtils.addValue(sb, historicalQuote.getVolume());
			sb.append(",").append(historicalQuote.getComment()).append("\n");
		}
		return sb;
	}

	public static List<ExtendedHistoricalQuote> sortQuoteList(final List<ExtendedHistoricalQuote> history) {
		Collections.sort(history, TimeseriesUtils.getComparator());
		return history;
	}
}
