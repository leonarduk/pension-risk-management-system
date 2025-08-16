package org.patriques.output.digitalcurrencies.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of json object, i.e:
 * "2014-04-01 17:59:59": {
 *   "1a. price (CNY)": "2813.95451515",
 *   "1b. price (USD)": "453.42760964",
 *   "2. volume": "5628.02114700",
 *   "3. market cap (USD)": "2596155.69911398"
 * }
 */
@AllArgsConstructor
@Getter
public class SimpelDigitalCurrencyData {

  private final LocalDateTime dateTime;
  private final double priceA;
  private final double priceB;
  private final double volume;
  private final double marketCap;
}
