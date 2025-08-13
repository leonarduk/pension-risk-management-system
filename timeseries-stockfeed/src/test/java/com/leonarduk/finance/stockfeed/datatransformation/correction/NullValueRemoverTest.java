package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.utils.TimeseriesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class NullValueRemoverTest {

    @Test
    public void removesZeroClosePrices() {
        List<Bar> history = Arrays.asList(
                TimeseriesUtils.createSyntheticBar(LocalDate.of(2020,1,1), 10.0, 10.0, ""),
                TimeseriesUtils.createSyntheticBar(LocalDate.of(2020,1,2), 0.0, 0.0, "")
        );
        List<Bar> cleaned = new NullValueRemover().clean(history);
        Assert.assertEquals(1, cleaned.size());
        Assert.assertEquals(10.0, cleaned.get(0).getClosePrice().doubleValue(), 0.0001);
    }
}
