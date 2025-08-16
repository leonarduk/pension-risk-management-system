package org.patriques.output.technicalindicators;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response from technical indicator call. Each specific response, i.e EMA, MACD, etc, extends this class.
 * This class simply acts as a container of metadata and indicator data.
 *
 * @param <Data> the data contained in the response
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class TechnicalIndicatorResponse<Data> {

  private final Map<String, String> metaData;
  private final List<Data> indicatorData;
}
