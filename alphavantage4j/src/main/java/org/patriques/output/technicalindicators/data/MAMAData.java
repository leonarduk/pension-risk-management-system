package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of MAMA indicator json objects, i.e:
 * "2017-12-01 16:00":
 *   "FAMA": "52.4939",
 *   "MAMA": "80.9751"
 * }
 */
@AllArgsConstructor
@Getter
public class MAMAData {
  private final LocalDateTime datetime;
  private final double fama;
  private final double mama;
}

