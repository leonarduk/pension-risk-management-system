package org.patriques.output.exchange;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ilker Kopan
 */
@AllArgsConstructor
@Getter
public class ForexResponse<Data> {
    private final Map<String, String> metaData;
    private final List<Data> forexData;
}
