package org.patriques.output.digitalcurrencies.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of json object, i.e:
 * "2014-04-01": {
 *   "1a. open (CNY)": "2813.95451515",
 *   "1b. open (USD)": "453.42760964",
 *   "2a. high (CNY)": "2918.63938797",
 *   "2b. high (USD)": "470.55596761",
 *   "3a. low (CNY)": "2813.95451515",
 *   "3b. low (USD)": "453.42760964",
 *   "4a. close (CNY)": "2859.93706764",
 *   "4b. close (USD)": "461.29103486",
 *   "5. volume": "5628.02114700",
 *   "6. market cap (USD)": "2596155.69911398"
 * }
 */
@AllArgsConstructor
@Getter
public class DigitalCurrencyData {

  private final LocalDateTime dateTime;
  private final double openA;
  private final double openB;
  private final double highA;
  private final double highB;
  private final double lowA;
  private final double lowB;
  private final double closeA;
  private final double closeB;
  private final double volume;
  private final double marketCap;
}
