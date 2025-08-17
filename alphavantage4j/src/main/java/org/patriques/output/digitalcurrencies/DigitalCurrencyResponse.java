package org.patriques.output.digitalcurrencies;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DigitalCurrencyResponse<Data> {

  private final Map<String, String> metaData;
  private final List<Data> digitalData;
}
