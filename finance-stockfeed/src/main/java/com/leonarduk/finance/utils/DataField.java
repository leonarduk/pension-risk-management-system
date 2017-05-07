package com.leonarduk.finance.utils;

public class DataField {
	private final String name;
	private final Object value;
	private boolean display;

	public DataField(final String name, final Object value) {
		this(name, value, true);
	}

	public DataField(final String name, final Object value, final boolean display) {
		this.name = name;
		this.value = value;
		this.display = display;
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
