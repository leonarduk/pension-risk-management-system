package org.patriques.output.sectorperformances;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.JsonParser;
import org.patriques.output.sectorperformances.data.SectorData;

/**
 * Representation of sectors percentual change over different timeperiods
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class Sectors {
  private final Map<String, String> metaData;
  private final List<SectorData> sectors;

  /**
   * Create Sectors data representation from json object.
   *
   * @param json string to parse
   * @return {@code Sectors} data
   */
  public static Sectors from(String json) {
    SectorPerformancesParser parser = new SectorPerformancesParser();
    return parser.parseJson(json);
  }

  /**
   * Helper class for parsing json to {@code Sector}.
   *
   * @see JsonParser
   */
  private static class SectorPerformancesParser extends JsonParser<Sectors> {

    @Override
    public Sectors resolve(JsonObject rootObject) {
      Type dataType = new TypeToken<Map<String, Map<String, String>>>() {
      }.getType();
      try {
        Map<String, Map<String, String>> data = GSON.fromJson(rootObject, dataType);
        Map<String, String> metadata = data.remove("Meta Data");
        List<SectorData> sectors = new ArrayList<>();
        data.forEach((key, values) -> {
          try {
            sectors.add(createSectorData(key, values));
          } catch (ParseException e) {
            throw new AlphaVantageException("technical indicators api change", e);
          }
        });
        return new Sectors(metadata, sectors);
      } catch (JsonSyntaxException e) {
        throw new AlphaVantageException("technical indicators api change", e);
      }
    }

    /**
     * Helper method for creating a {@link SectorData} instance.
     *
     * @param key the key, i.e "Rank A: Real-Time Performance" in the json object
     * @param values a map of the key, values, i.e "Telecommunication Services": "1.52%"
     * @return a {@link SectorData} instance
     * @throws ParseException
     */
    private SectorData createSectorData(String key, Map<String, String> values) throws ParseException {
      return new SectorData(
              key,
              Double.parseDouble(values.getOrDefault("Information Technology", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Health Care", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Consumer Staples", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Real Estate", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Materials", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Consumer Discretionary", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Energy", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Financials", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Industrials", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Utilities", "0").trim().replace("%", "")),
              Double.parseDouble(values.getOrDefault("Telecommunication Services", "0").trim().replace("%", ""))
      );
    }
  }
}
