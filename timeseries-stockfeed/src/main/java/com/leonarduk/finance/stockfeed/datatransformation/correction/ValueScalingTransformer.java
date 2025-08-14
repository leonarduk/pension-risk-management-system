package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ValueScalingTransformer implements TimeSeriesCleaner {

    private final Num scalingFactor;
    private final Instrument instrument;

    public ValueScalingTransformer(Instrument instrument, double scalingFactor) {
        this.scalingFactor = DoubleNum.valueOf(scalingFactor);
        this.instrument = instrument;
    }

    @Override
    public List<Bar> clean(final List<Bar> history) {
        return TimeseriesUtils.sortQuoteList(
                history.stream()
                        .map(current -> new ExtendedHistoricalQuote("",
                                current.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(),

                                current.getOpenPrice().multipliedBy(scalingFactor),
                                current.getLowPrice().multipliedBy(scalingFactor),
                                current.getHighPrice().multipliedBy(scalingFactor),
                                current.getClosePrice().multipliedBy(scalingFactor),

                                current.getVolume().longValue(),
                                instrument.isin() + " scaled from " + current.getClosePrice() + " to "
                                        + current.getClosePrice().multipliedBy(scalingFactor))
                        )
                        .collect(Collectors.toCollection(LinkedList::new)));
    }

}
