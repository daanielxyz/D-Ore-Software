package com.dore.orders;

/** En orden de avance. CANCELLED queda por fuera del flujo. */
public enum OrderStatus {
    PENDING,
    IN_DESIGN,
    READY,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}
