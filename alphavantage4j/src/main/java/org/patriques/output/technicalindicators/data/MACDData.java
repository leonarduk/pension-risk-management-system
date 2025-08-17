package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of MAMA indicator json objects, i.e:
 * "2017-12-01 16:00":
 *   "MACD_Signal": "-0.0265",
 *   "MACD_Hist": "-0.0074",
 *   "MACD": "-0.0339"
 * }
 */
@AllArgsConstructor
@Getter
public class MACDData {
  private final LocalDateTime datetime;
  private final double signal;
  private final double hist;
  private final double macd;
}
