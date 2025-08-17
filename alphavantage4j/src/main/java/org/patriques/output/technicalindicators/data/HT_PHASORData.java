package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of Hilbert transform, phasor components indicator json objects, i.e:
 * "2000-01-24": {
 *   "PHASE": "-2.5430",
 *   "QUADRATURE": "-10.8250"
 * }
 */
@AllArgsConstructor
@Getter
public class HT_PHASORData {
  private final LocalDateTime datetime;
  private final double phase;
  private final double quadrature;
}

