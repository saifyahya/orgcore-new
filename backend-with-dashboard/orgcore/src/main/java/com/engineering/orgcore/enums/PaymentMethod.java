package com.engineering.orgcore.enums;

public enum PaymentMethod {
    CASH, CARD, TRANSFER, OTHER;

    public static PaymentMethod fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (PaymentMethod type : PaymentMethod.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid PaymentMethod: " + value);
    }
}


