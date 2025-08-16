package org.patriques.output.digitalcurrencies;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.JsonParser;

/**
 * Since the format for the digital and crypto currency responses differ slightly but on the whole
 * have the same structure the {@code DigitalCurrencyParser} extracts the similarity of
 * the parsing to this class.
 *
 * @see JsonParser
 * @param <Response> the response for each individual API call, i.e Intraday, Daily etc.
 */
abstract class DigitalCurrencyParser<Response> extends JsonParser<Response> {

  /**
   * The specifics of the resolution is pushed down to each response type, i.e Intraday, Daily etc.
   *
   * @param metaData the meta data
   * @param digitalCurrencyData the digital currency data
   * @return the response for each individual response, i.e Intraday, Daily etc.
   */
  abstract Response resolve(Map<String, String> metaData,
                            Map<String, Map<String, String>> digitalCurrencyData) ;

  /**
   * Gets the key for the digital currency data, this differs for each response type, i.e Intraday, Daily etc.
   * This is used by the resolve method below.
   *
   * @return the digital currency data key
   */
  abstract String getDigitalCurrencyDataKey();

  @Override
  public Response resolve(JsonObject rootObject)  {
    Type metaDataType = new TypeToken<Map<String, String>>() {
    }.getType();
    Type responseType = new TypeToken<Map<String, Map<String, String>>>() {
    }.getType();
    try {
      Map<String, String> metaData = GSON.fromJson(rootObject.get("Meta Data"), metaDataType);
      Map<String, Map<String, String>> digitalCurrencyData = GSON.fromJson(rootObject.get(getDigitalCurrencyDataKey()), responseType);
      return resolve(metaData, digitalCurrencyData);
    } catch (JsonSyntaxException e) {
      throw new AlphaVantageException("time series api change", e);
    }
  }

}
