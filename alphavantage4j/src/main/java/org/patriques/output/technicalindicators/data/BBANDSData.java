package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of Bollinger bands indicator json objects, i.e:
 * "2017-12-01 16:00":
 * "Real Lower Band": "89.2034",
 * "Real Upper Band": "119.0966",
 * "Real Middle Band": "104.1500"
 * }
 */
@AllArgsConstructor
@Getter
public class BBANDSData {
  private final LocalDateTime datetime;
  private final double lowerBand;
  private final double upperBand;
  private final double midBand;
}
