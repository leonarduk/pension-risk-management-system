package org.patriques;

import org.junit.Test;
import org.patriques.input.ApiParameter;
import org.patriques.input.ApiParameterBuilder;
import org.patriques.input.Symbol;

import static org.junit.Assert.*;

public class ApiParameterBuilderTest {

    @Test
    public void testAppendParametersAndBuildUrl() {
        ApiParameterBuilder builder = new ApiParameterBuilder();
        builder.append(new Symbol("IBM"));
        builder.append("datatype", "json");
        assertEquals("&symbol=IBM&datatype=json", builder.getUrl());
    }

    @Test
    public void testAppendNullParameterIsIgnored() {
        ApiParameterBuilder builder = new ApiParameterBuilder();
        builder.append((ApiParameter) null);
        assertEquals("", builder.getUrl());
    }
}
