package com.leonarduk.finance.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import com.google.common.collect.Sets;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.interpolation.BadDateRemover;
import com.leonarduk.finance.stockfeed.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.interpolation.LinearInterpolator;

public class TimeseriesUtils {
	public static int cleanUpSeries(final Optional<StockV1> liveData) throws IOException {
		final List<Bar> history = liveData.get().getHistory();
		final int original = history.size();
		final List<Bar> clean = new BadDateRemover().clean(history);
		liveData.get().setHistory(clean);
		final int fixed = clean.size();
		return original - fixed;
	}

	public static boolean containsDatePoints(final List<Bar> cachedHistory, final LocalDate... dates) {
		final Set<LocalDate> dateSet = Sets.newHashSet(dates);
		return (cachedHistory.stream().filter(quote -> dateSet.contains(quote.getEndTime()))
				.collect(Collectors.toList()).size() == dateSet.size());
	}

	private static Double ensureIsDouble(final Num num) {
		if (num == null) {
			return null;
		}
		return num.doubleValue();
	}

	public static Comparator<? super Bar> getComparator() {
		final Comparator<? super Bar> comparator = (o1, o2) -> {
			return o2.getEndTime().compareTo(o1.getEndTime());
		};
		return comparator;
	}

	public static Bar getMostRecentQuote(final List<Bar> list) {
		return list.get(0);
	}

	public static Bar getOldestQuote(final List<Bar> history) {
		return history.get(history.size() - 1);
	}

	public static TimeSeries getTimeSeries(final StockV1 stock, final int i) throws IOException {
		return TimeseriesUtils.getTimeSeries(stock, LocalDate.now().minusYears(i), LocalDate.now());
	}

	public static TimeSeries getTimeSeries(final StockV1 stock, final LocalDate fromDate, final LocalDate toDate)
			throws IOException {
		List<Bar> history = stock.getHistory();
		if ((null == history) || history.isEmpty()) {
			final Optional<StockV1> optional = new IntelligentStockFeed().get(stock.getInstrument(), fromDate, toDate);
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
		return new LinearInterpolator().interpolate(new BaseTimeSeries(stock.getName(), ticks));
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
			final Double newOpenPriceRaw) {

		final Double newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
		final Double newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);
		return new ExtendedHistoricalQuote("", currentDate, BigDecimal.valueOf(newOpenPrice),
				BigDecimal.valueOf(Double.min(newClosePrice, newOpenPrice)), BigDecimal.valueOf(newClosePrice),
				BigDecimal.valueOf(Double.max(newClosePrice, newOpenPrice)), BigDecimal.valueOf(newClosePrice),
				DoubleNum.valueOf(0), "");
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

	private static Num ifNull(final Num num, final Num defaultValue) {
		if (num == null) {
			return defaultValue;
		}
		return num;
	}

	public static Optional<StockV1> interpolateAndSortSeries(final LocalDate fromLocalDate, final LocalDate toLocalDate,
			final boolean interpolate, final Optional<StockV1> liveData) throws IOException {
		List<Bar> history = TimeseriesUtils.sortQuoteList(liveData.get().getHistory());
		if (interpolate) {
			final LinearInterpolator linearInterpolator = new LinearInterpolator();
			final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();

			history = linearInterpolator.interpolate(flatLineInterpolator
					.extendToToDate(flatLineInterpolator.extendToFromDate(history, fromLocalDate), toLocalDate));
		}
		final List<Bar> subSeries = history.stream()
				.filter(q -> (q.getEndTime().toLocalDate().isAfter(fromLocalDate)
						&& q.getEndTime().toLocalDate().isBefore(toLocalDate))
						|| q.getEndTime().toLocalDate().isEqual(fromLocalDate)
						|| q.getEndTime().toLocalDate().isEqual(toLocalDate))
				.collect(Collectors.toList());
		TimeseriesUtils.sortQuoteList(subSeries);
		liveData.get().setHistory(subSeries);
		return liveData;
	}

	public static StringBuilder seriesToCsv(final List<Bar> series) {
		final StringBuilder sb = new StringBuilder("date,open,high,low,close,volume\n");
		for (final Bar historicalQuote : series) {
			sb.append(historicalQuote.getEndTime().toString());
			StringUtils.addValue(sb, historicalQuote.getOpenPrice());
			StringUtils.addValue(sb, historicalQuote.getMaxPrice());
			StringUtils.addValue(sb, historicalQuote.getMinPrice());
			StringUtils.addValue(sb, historicalQuote.getClosePrice());
			StringUtils.addValue(sb, historicalQuote.getVolume());
//			sb.append(",").append(historicalQuote.getComment())
			sb.append("\n");
		}
		return sb;
	}

	public static List<Bar> sortQuoteList(final List<Bar> history) {
		Collections.sort(history, TimeseriesUtils.getComparator());
		return history;
	}
}
