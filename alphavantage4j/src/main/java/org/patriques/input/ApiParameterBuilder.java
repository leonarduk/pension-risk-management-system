package org.patriques.input;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.Nullable;

/**
 * Builder for api parameters.
 */
public class ApiParameterBuilder {
  private StringBuilder urlBuilder;

  public ApiParameterBuilder() {
    this.urlBuilder = new StringBuilder();
  }

  /**
   * Append an api parameter to the builder.
   *
   * @param apiParameter the api parameter to append to the url.
   * @return this builder for method chaining.
   */
  public ApiParameterBuilder append(@Nullable ApiParameter apiParameter) {
    if (apiParameter != null) {
      append(apiParameter.getKey(), apiParameter.getValue());
    }
    return this;
  }

  /**
   * Append raw string parameters to the builder.
   * <p>
   * Both {@code key} and {@code value} are URL encoded before being appended.
   *
   * @param key in the api parameter key value pair.
   * @param value in the api parameter key value pair.
   * @return this builder for method chaining.
   */
  public ApiParameterBuilder append(String key, String value) {
    String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
    String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
    String parameter = "&" + encodedKey + "=" + encodedValue;
    this.urlBuilder.append(parameter);
    return this;
  }

  /**
   * Build the url string for the query in the api call.
   *
   * @return the url query string.
   */
  public String getUrl() {
    return this.urlBuilder.toString();
  }
}
