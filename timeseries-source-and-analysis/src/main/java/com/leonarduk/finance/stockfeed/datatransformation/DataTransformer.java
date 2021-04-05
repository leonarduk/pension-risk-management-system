package com.leonarduk.finance.stockfeed.datatransformation;

import java.io.IOException;
import java.util.List;

import org.ta4j.core.Bar;

public interface DataTransformer {
	List<Bar> transform(List<Bar> history) throws IOException;
}
