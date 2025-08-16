package org.patriques.output.exchange.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CurrencyExchangeData class used to register the rate of a conversion between to currencies.
 */
@AllArgsConstructor
@Getter
public class CurrencyExchangeData {

  private final String fromCurrencyCode;
  private final String fromCurrencyName;
  private final String toCurrencyCode;
  private final String toCurrencyName;
  private final float exchangeRate;
  private final LocalDateTime time;
  private final String timezone;
}
