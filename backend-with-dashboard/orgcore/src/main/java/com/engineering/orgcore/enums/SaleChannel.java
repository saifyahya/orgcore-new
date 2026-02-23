package com.engineering.orgcore.enums;

public enum SaleChannel {
    MANUAL, IMPORT, API, POS;

    public static SaleChannel fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (SaleChannel type : SaleChannel.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid SaleChannel: " + value);
    }
}