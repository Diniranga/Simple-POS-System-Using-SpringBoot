package com.ead.orderserver;

public enum OrderStatus {
    IN_QUEUE,
    PACKING,
    READY_TO_DISPATCH,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

