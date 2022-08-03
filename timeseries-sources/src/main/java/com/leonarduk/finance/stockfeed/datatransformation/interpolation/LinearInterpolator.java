package com.leonarduk.finance.stockfeed.datatransformation.interpolation;

import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LinearInterpolator extends AbstractLineInterpolator {

    @Override
    protected Bar calculateFutureValue(final Bar lastQuote, final LocalDate today) {
        // TODO maybe use a gradient from a few points before
        throw new UnsupportedOperationException();
    }

    @Override
    protected Bar calculatePastValue(final Bar firstQuote, final LocalDate fromDate) {
        // TODO maybe use a gradient from a few points before
        throw new UnsupportedOperationException();
    }

    @Override
    public Bar createSyntheticQuote(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote)
            throws IOException {
        final double timeInteval = DateUtils.getDiffInWorkDays(nextQuote.getEndTime().toLocalDate(),
                currentQuote.getEndTime().toLocalDate());
        final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getEndTime().toLocalDate(), currentDate);
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
                currentQuote.getEndTime().toLocalDate(), nextQuote.getEndTime().toLocalDate());
        final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getEndTime().toLocalDate(), currentDate);
        final double multiplier = dayCount / timeInterval;

        final Double changeClosePrice = nextQuote.getClosePrice().doubleValue()
                - currentQuote.getClosePrice().doubleValue();
        final Double changeOpenPrice = nextQuote.getOpenPrice().doubleValue()
                - currentQuote.getOpenPrice().doubleValue();

        final Double newClosePrice = currentQuote.getClosePrice().doubleValue()
                + (changeClosePrice * Double.valueOf(multiplier));
        final Double newOpenPrice = currentQuote.getOpenPrice().doubleValue()
                + (changeOpenPrice * Double.valueOf(multiplier));

        return TimeseriesUtils.createSyntheticBar(currentDate, newClosePrice, newOpenPrice, "Interpolated from "
                + currentQuote.getEndTime().toLocalDate() + " - " + nextQuote.getEndTime().toLocalDate());
    }

}
