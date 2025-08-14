package com.leonarduk.finance.stockfeed.datatransformation.interpolation;

import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class LinearInterpolator extends AbstractLineInterpolator {

    private List<Bar> workingSeries;

    @Override
    public List<Bar> extendToToDate(final List<Bar> series, final LocalDate toLocalDate) throws IOException {
        this.workingSeries = series;
        TimeseriesUtils.sortQuoteList(series);
        return super.extendToToDate(series, toLocalDate);
    }

    @Override
    public List<Bar> extendToFromDate(final List<Bar> series, final LocalDate fromDate) throws IOException {
        this.workingSeries = series;
        TimeseriesUtils.sortQuoteList(series);
        return super.extendToFromDate(series, fromDate);
    }

    @Override
    protected Bar calculateFutureValue(final Bar lastQuote, final LocalDate today) {
        Bar previous = null;
        if (this.workingSeries != null && this.workingSeries.size() >= 2) {
            previous = this.workingSeries.get(this.workingSeries.size() - 2);
        }
        if (previous == null) {
            return new ExtendedHistoricalQuote(lastQuote, today,
                    "Copied from " + lastQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        double interval = DateUtils.getDiffInWorkDays(lastQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                previous.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        if (interval == 0) {
            return new ExtendedHistoricalQuote(lastQuote, today,
                    "Copied from " + lastQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        double multiplier = DateUtils.getDiffInWorkDays(today, lastQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate()) / interval;

        Num changeClosePrice = lastQuote.getClosePrice().minus(previous.getClosePrice());
        Num changeOpenPrice = lastQuote.getOpenPrice().minus(previous.getOpenPrice());

        Num newClosePrice = lastQuote.getClosePrice()
                .plus(changeClosePrice.multipliedBy(DoubleNum.valueOf(multiplier)));
        Num newOpenPrice = lastQuote.getOpenPrice()
                .plus(changeOpenPrice.multipliedBy(DoubleNum.valueOf(multiplier)));

        try {
            return TimeseriesUtils.createSyntheticQuote(lastQuote, today,
                    BigDecimal.valueOf(newClosePrice.doubleValue()),
                    BigDecimal.valueOf(newOpenPrice.doubleValue()),
                    "Extrapolated from " + previous.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate() + " to "
                            + lastQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Bar calculatePastValue(final Bar firstQuote, final LocalDate fromDate) {
        Bar next = null;
        if (this.workingSeries != null && this.workingSeries.size() >= 2) {
            int index = this.workingSeries.indexOf(firstQuote);
            if (index < 0) {
                index = 0;
            }
            if (index + 1 < this.workingSeries.size()) {
                next = this.workingSeries.get(index + 1);
            }
        }
        if (next == null) {
            try {
                return TimeseriesUtils.createSyntheticQuote(firstQuote, fromDate,
                        BigDecimal.valueOf(firstQuote.getClosePrice().doubleValue()),
                        BigDecimal.valueOf(firstQuote.getOpenPrice().doubleValue()),
                        "Copied from " + firstQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        double interval = DateUtils.getDiffInWorkDays(next.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                firstQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        if (interval == 0) {
            try {
                return TimeseriesUtils.createSyntheticQuote(firstQuote, fromDate,
                        BigDecimal.valueOf(firstQuote.getClosePrice().doubleValue()),
                        BigDecimal.valueOf(firstQuote.getOpenPrice().doubleValue()),
                        "Copied from " + firstQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        double multiplier = DateUtils.getDiffInWorkDays(firstQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(), fromDate) / interval;

        Num changeClosePrice = next.getClosePrice().minus(firstQuote.getClosePrice());
        Num changeOpenPrice = next.getOpenPrice().minus(firstQuote.getOpenPrice());

        Num newClosePrice = firstQuote.getClosePrice()
                .minus(changeClosePrice.multipliedBy(DoubleNum.valueOf(multiplier)));
        Num newOpenPrice = firstQuote.getOpenPrice()
                .minus(changeOpenPrice.multipliedBy(DoubleNum.valueOf(multiplier)));

        try {
            return TimeseriesUtils.createSyntheticQuote(firstQuote, fromDate,
                    BigDecimal.valueOf(newClosePrice.doubleValue()),
                    BigDecimal.valueOf(newOpenPrice.doubleValue()),
                    "Extrapolated from " + firstQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate() + " to "
                            + next.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Bar createSyntheticQuote(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote)
            throws IOException {
        final double timeInteval = DateUtils.getDiffInWorkDays(nextQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                currentQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(), currentDate);
        final double multiplier = dayCount / timeInteval;

        final Num changeClosePrice = nextQuote.getClosePrice().minus(currentQuote.getClosePrice());
        final Num changeOpenPrice = nextQuote.getOpenPrice().minus(currentQuote.getOpenPrice());

        final Num newClosePrice = currentQuote.getClosePrice()
                .plus(changeClosePrice.multipliedBy(DoubleNum.valueOf(multiplier)));
        final Num newOpenPrice = currentQuote.getOpenPrice()
                .plus(changeOpenPrice.multipliedBy(DoubleNum.valueOf(multiplier)));

        return TimeseriesUtils.createSyntheticQuote(currentQuote, currentDate,
                BigDecimal.valueOf(newClosePrice.doubleValue()), BigDecimal.valueOf(newOpenPrice.doubleValue()),
                "Interpolated from " + currentQuote.getEndTime() + "(" + currentQuote.getClosePrice() + ") to "
                        + nextQuote.getEndTime() + " (" + nextQuote.getClosePrice() + ")");
    }

    @Override
    public Bar createSyntheticBar(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote) {

        final double timeInterval = DateUtils.getDiffInWorkDays(
                currentQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(), nextQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(), currentDate);
        final double multiplier = dayCount / timeInterval;

        final double changeClosePrice = nextQuote.getClosePrice().doubleValue()
                - currentQuote.getClosePrice().doubleValue();
        final double changeOpenPrice = nextQuote.getOpenPrice().doubleValue()
                - currentQuote.getOpenPrice().doubleValue();

        final Double newClosePrice = currentQuote.getClosePrice().doubleValue()
                + (changeClosePrice * multiplier);
        final Double newOpenPrice = currentQuote.getOpenPrice().doubleValue()
                + (changeOpenPrice * multiplier);

        return TimeseriesUtils.createSyntheticBar(currentDate, newClosePrice, newOpenPrice, "Interpolated from "
                + currentQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate() + " - " + nextQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
    }

}
