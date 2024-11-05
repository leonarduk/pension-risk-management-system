package com.leonarduk.finance.stockfeed.datatransformation;

import org.ta4j.core.Bar;

import java.io.IOException;
import java.util.List;

public interface DataTransformer {
    List<Bar> transform(List<Bar> history) throws IOException;
}
