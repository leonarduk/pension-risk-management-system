package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of AROON indicator json objects, i.e:
 * "2000-01-24": {
 *   "Aroon Up": "0.0000",
 *   "Aroon Down": "100.0000"
 * }
 */
@AllArgsConstructor
@Getter
public class AROONData {
  private final LocalDateTime datetime;
  private final double aroonUp;
  private final double aroonDown;
}

