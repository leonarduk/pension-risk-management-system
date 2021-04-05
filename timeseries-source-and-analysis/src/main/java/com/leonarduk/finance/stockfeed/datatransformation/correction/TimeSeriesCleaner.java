package com.leonarduk.finance.stockfeed.datatransformation.correction;

import java.util.List;

import org.ta4j.core.Bar;

import com.leonarduk.finance.stockfeed.datatransformation.DataTransformer;

public interface TimeSeriesCleaner extends DataTransformer {

	@Override
	default List<Bar> transform(List<Bar> history) {
		return clean(history);

	}

	List<Bar> clean(List<Bar> history);

}
