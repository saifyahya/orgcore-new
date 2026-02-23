package com.engineering.orgcore.enums;

public enum ReferenceType {
    SALE,
    TRANSFER,
    PURCHASE,
    MANUAL,
    IMPORT;

    public static ReferenceType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (ReferenceType type : ReferenceType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid ReferenceType: " + value);
    }
}