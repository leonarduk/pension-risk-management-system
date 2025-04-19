package com.leonarduk.finance.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataField {
    private boolean display;
    private final String name;
    private final Object value;

    @JsonIgnore
    private final ValueFormatter formatter;

    public DataField(final String name, final Object value) {
        this(name, value, (Object::toString), true);
    }

    public DataField(final String name, final Object value,
                     final ValueFormatter formatter, final boolean display) {
        this.name = name;
        this.value = value;
        this.display = display;
        this.formatter = formatter;
    }

    public ValueFormatter getFormatter() {
        return this.formatter;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isDisplay() {
        return this.display;
    }

}
