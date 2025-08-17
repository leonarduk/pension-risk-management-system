package org.patriques;

import org.junit.jupiter.api.Test;
import org.patriques.input.ApiParameter;
import org.patriques.input.ApiParameterBuilder;
import org.patriques.input.Symbol;

import static org.junit.jupiter.api.Assertions.*;

public class ApiParameterBuilderTest {

    @Test
    public void testAppendParametersAndBuildUrl() {
        ApiParameterBuilder builder = new ApiParameterBuilder();
        builder.append(new Symbol("IBM")).append("datatype", "json");
        assertEquals("&symbol=IBM&datatype=json", builder.getUrl());
    }

    @Test
    public void testAppendNullParameterIsIgnored() {
        ApiParameterBuilder builder = new ApiParameterBuilder();
        builder.append((ApiParameter) null);
        assertEquals("", builder.getUrl());
    }

    @Test
    public void testAppendEncodesParameters() {
        ApiParameterBuilder builder = new ApiParameterBuilder();
        builder.append("sp ce", "a+b");
        assertEquals("&sp+ce=a%2Bb", builder.getUrl());
    }
}
