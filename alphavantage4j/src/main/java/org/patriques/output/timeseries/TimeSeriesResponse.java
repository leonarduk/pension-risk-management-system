package org.patriques.output.timeseries;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.patriques.output.timeseries.data.StockData;

/**
 * Response from time series call. Each specific response, i.e IntraDay, Daily, etc, extends this class.
 * This class simply acts as a container of metadata and stockdata.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class TimeSeriesResponse {

  private final Map<String, String> metaData;
  private final List<StockData> stockData;
}
