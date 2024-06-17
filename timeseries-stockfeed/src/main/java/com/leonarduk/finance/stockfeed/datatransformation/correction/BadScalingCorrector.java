package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class BadScalingCorrector implements TimeSeriesCleaner {

    @Override
    public List<Bar> clean(List<Bar> history) {
        if (history.isEmpty())
            return history;

        List<Bar> cleanedSeries = Lists.newArrayList();

        Iterator<Bar> iter = history.iterator();
        Bar previous = iter.next();
        cleanedSeries.add(previous);
        while (iter.hasNext()) {
            Bar current = iter.next();
            String comment = "";
            if (current instanceof ExtendedHistoricalQuote) {
                comment = ((ExtendedHistoricalQuote) current).getComment();
            }
            int SCALE = 80;
            if (current.getClosePrice().dividedBy(DoubleNum.valueOf(SCALE)).isGreaterThan(previous.getClosePrice())) {
                try {
                    ExtendedHistoricalQuote cleanedQuote = new ExtendedHistoricalQuote("",
                            current.getEndTime().toLocalDate(),

                            scaleDown(current.getOpenPrice()),
                            scaleDown(current.getMinPrice()),
                            scaleDown(current.getMaxPrice()),
                            scaleDown(current.getClosePrice()),

                            current.getVolume(),
                            comment + ": Scaled from " + current.getClosePrice() + " to " + scaleDown(current.getClosePrice()));
                    cleanedSeries.add(cleanedQuote);
                } catch (IOException e) {
                    cleanedSeries.add(current);
                    e.printStackTrace();
                }
            } else if (current.getClosePrice().multipliedBy(DoubleNum.valueOf(SCALE))
                    .isLessThan(previous.getClosePrice())) {
                try {
                    ExtendedHistoricalQuote cleanedQuote = new ExtendedHistoricalQuote("",
                            current.getEndTime().toLocalDate(),

                            scaleUp(current.getOpenPrice()),
                            scaleUp(current.getMinPrice()),
                            scaleUp(current.getMaxPrice()),
                            scaleUp(current.getClosePrice()),

                            current.getVolume(),
                            "Scaled from " + current.getClosePrice() + " to " + scaleUp(current.getClosePrice()));
                    cleanedSeries.add(cleanedQuote);
                } catch (IOException e) {
                    cleanedSeries.add(current);
                    e.printStackTrace();
                }
            } else {
                cleanedSeries.add(current);
            }
        }

        return cleanedSeries;
    }

    private Num scaleUp(Num orignal) {
        return orignal.multipliedBy(DoubleNum.valueOf(100));
    }

    public Num scaleDown(Num orignal) {
        return orignal.dividedBy(DoubleNum.valueOf(100));
    }

}
