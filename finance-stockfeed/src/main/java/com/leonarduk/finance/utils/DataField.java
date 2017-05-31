package com.leonarduk.finance.utils;

public class DataField {
	private boolean					display;
	private final ValueFormatter	formatter;
	private final String			name;
	private final Object			value;

	public DataField(final String name, final Object value) {
		this(name, value, (Object::toString), true);
	}

	public DataField(final String name, final Object value,
	        final boolean display) {
		this(name, value, (Object::toString), display);
	}

	public DataField(final String name, final Object value,
	        final ValueFormatter formatter) {
		this(name, value, formatter, true);
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

	public void hide() {
		this.display = false;
	}

	public boolean isDisplay() {
		return this.display;
	}

}
