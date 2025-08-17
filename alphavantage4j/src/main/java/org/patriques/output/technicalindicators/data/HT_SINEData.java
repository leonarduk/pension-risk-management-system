package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of Hilbert transform, sine wave indicator json objects, i.e:
 * "2000-01-24": {
 *   "LEAD SINE": "-0.8497",
 *   "SINE": "-0.9737"
 * }
 */
@AllArgsConstructor
@Getter
public class HT_SINEData {
  private final LocalDateTime datetime;
  private final double leadSine;
  private final double sine;
}
