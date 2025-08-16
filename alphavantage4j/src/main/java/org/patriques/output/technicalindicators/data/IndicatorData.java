package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of simple indicator json objects, i.e:
 * "2017-12-01 16:00": {
 *   "EMA": "84.0203"
 * }
 */
@AllArgsConstructor
@Getter
public class IndicatorData {
  private final LocalDateTime datetime;
  private final double data;
}
