package org.unibl.etf.mdp.model;

public enum StatusEnum {
    PENDING, APPROVED, REJECTED, BLOCKED;

    public static StatusEnum fromString(String value) {
        return valueOf(value.toUpperCase());
    }
}

