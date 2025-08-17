package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of STOCH indicator json objects, i.e:
 * "2017-12-01 16:00":
 * "SlowD": "31.4081",
 * "SlowK": "31.4081"
 * }
 */
@AllArgsConstructor
@Getter
public class STOCHDataSlow {
  private final LocalDateTime datetime;
  private final double slowD;
  private final double slowK;
}
