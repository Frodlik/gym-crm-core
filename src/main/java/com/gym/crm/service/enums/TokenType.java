package com.gym.crm.service.enums;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public static TokenType fromValue(String value) {
        for (TokenType type : TokenType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown token type: " + value);
    }
}
