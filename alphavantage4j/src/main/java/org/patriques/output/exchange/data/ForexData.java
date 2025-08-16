package org.patriques.output.exchange.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of json object, i.e:
 * "2018-12-25": {
 *             "1. open": "1.1413",
 *             "2. high": "1.1422",
 *             "3. low": "1.1364",
 *             "4. close": "1.1377"
 *         }
 *
 * @author ilker Kopan
 */
@AllArgsConstructor
@Getter
public class ForexData {
    private final LocalDateTime dateTime;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
}
