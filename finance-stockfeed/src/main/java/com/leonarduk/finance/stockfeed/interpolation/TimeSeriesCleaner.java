package com.leonarduk.finance.stockfeed.interpolation;

import java.util.List;

import org.ta4j.core.Bar;

public interface TimeSeriesCleaner {

	List<Bar> clean(List<Bar> history);

}
