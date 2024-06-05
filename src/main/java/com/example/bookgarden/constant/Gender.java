package com.example.bookgarden.constant;

public enum Gender {
    MALE, FEMALE, OTHER;
    public static Gender fromString(String value) {
        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
