package com.leonarduk.finance.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

class RiskEndpointTest {

    @Test
    void usesLangParameterForLocaleSpecificMessage() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(eq("returns.empty"), isNull(), eq(Locale.FRENCH)))
                .thenReturn("Aucune donnée de rendement fournie");

        RiskEndpoint endpoint = new RiskEndpoint(messageSource);

        Map<String, List<Double>> body = Map.of("returns", Collections.emptyList());

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> endpoint.historicVar(body, 0.95, null, "fr"));

        assertEquals("Aucune donnée de rendement fournie", ex.getReason());
    }

    @Test
    void usesAcceptLanguageHeaderForLocaleSpecificMessage() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(eq("returns.empty"), isNull(), eq(Locale.GERMAN)))
                .thenReturn("Keine Renditedaten");

        RiskEndpoint endpoint = new RiskEndpoint(messageSource);

        Map<String, List<Double>> body = Map.of("returns", Collections.emptyList());

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> endpoint.historicVar(body, 0.95, "de", null));

        assertEquals("Keine Renditedaten", ex.getReason());
    }
}
