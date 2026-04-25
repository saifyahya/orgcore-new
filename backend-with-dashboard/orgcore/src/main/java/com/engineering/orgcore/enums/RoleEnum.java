package com.engineering.orgcore.enums;

public enum RoleEnum {
    ADMIN("ADMIN"),
    CASHER("CASHER");

    String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public static RoleEnum fromValue(String role) {
        for (RoleEnum r : RoleEnum.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("No enum constant " + role);
    }

    public static String toValue(RoleEnum role) {
        return role.value;
    }
}
