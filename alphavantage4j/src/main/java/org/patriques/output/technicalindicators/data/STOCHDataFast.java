package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of STOCH indicator json objects, i.e:
 * "2017-12-01 16:00":
 *   "FastD": "25.7924",
 *   "FastK": "18.4211"
 * }
 */
@AllArgsConstructor
@Getter
public class STOCHDataFast {
  private final LocalDateTime datetime;
  private final double fastK;
  private final double fastD;
}
