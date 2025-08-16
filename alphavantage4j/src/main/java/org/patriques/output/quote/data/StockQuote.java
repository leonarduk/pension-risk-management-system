package org.patriques.output.quote.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of json object, i.e:
 * {
 *   "1. symbol": "MSFT",
 *   "2. price": "96.3850",
 *   "3. volume": "--",
 *   "4. timestamp": "2018-05-18 15:59:48"
 * }
 */
@AllArgsConstructor
@Getter
public class StockQuote {
  private final String symbol;
  private final double price;
  private final long volume;
  private final LocalDateTime timestamp;
}
