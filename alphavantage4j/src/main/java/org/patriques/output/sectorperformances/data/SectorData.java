package org.patriques.output.sectorperformances.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of a json object, i.e:
 * "Rank A: Real-Time Performance": {
 *  "Telecommunication Services": "1.52%",
 *  "Health Care": "1.11%",
 *  "Energy": "0.85%",
 *  "Financials": "0.59%",
 *  "Consumer Discretionary": "0.43%",
 *  "Information Technology": "0.42%",
 *  "Industrials": "0.40%",
 *  "Utilities": "0.32%",
 *  "Real Estate": "0.25%",
 *  "Consumer Staples": "0.22%",
 *  "Materials": "-0.02%"
 * }
 */
@AllArgsConstructor
@Getter
public class SectorData {

  private final String key;
  private final double informationTechnology;
  private final double healthCare;
  private final double consumerStaples;
  private final double realEstate;
  private final double materials;
  private final double consumerDiscretionary;
  private final double energy;
  private final double financials;
  private final double industrials;
  private final double utilities;
  private final double telecommunicationServices;
}
