package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ValueScalingTransformerTest {

    @Test
    public void scalesPricesByFactor() {
        List<Bar> history = Collections.singletonList(
                TimeseriesUtils.createSyntheticBar(LocalDate.of(2020,1,1), 2.0, 2.0, "")
        );
        ValueScalingTransformer transformer = new ValueScalingTransformer(Instrument.CASH, 10.0);
        List<Bar> scaled = transformer.clean(history);
        Assert.assertEquals(1, scaled.size());
        Assert.assertEquals(20.0, scaled.get(0).getClosePrice().doubleValue(), 0.0001);
    }
}
