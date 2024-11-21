package org.unibl.etf.mdp.model;

public enum StatusEnum {
	PENDING;

	public static StatusEnum fromString(String value) {
		return valueOf(value.toUpperCase());
	}
}
