package com.example.bookgarden.constant;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    DELIVERING,
    DELIVERED,
    CONFIRMED,
    CANCELLED;
    public static OrderStatus fromString(String status) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.name().equalsIgnoreCase(status)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus: " + status);
    }

}

