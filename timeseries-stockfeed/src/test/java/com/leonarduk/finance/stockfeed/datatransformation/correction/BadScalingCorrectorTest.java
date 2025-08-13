package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.utils.TimeseriesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class BadScalingCorrectorTest {

    @Test
    public void scalesValuesWhenGapTooLarge() {
        List<Bar> history = Arrays.asList(
                TimeseriesUtils.createSyntheticBar(LocalDate.of(2020,1,1), 100.0, 100.0, ""),
                TimeseriesUtils.createSyntheticBar(LocalDate.of(2020,1,2), 10000.0, 10000.0, ""),
                TimeseriesUtils.createSyntheticBar(LocalDate.of(2020,1,3), 1.0, 1.0, "")
        );
        List<Bar> cleaned = new BadScalingCorrector().clean(history);
        Assert.assertEquals(3, cleaned.size());
        Assert.assertEquals(100.0, cleaned.get(1).getClosePrice().doubleValue(), 0.0001);
        Assert.assertEquals(100.0, cleaned.get(2).getClosePrice().doubleValue(), 0.0001);
    }
}
