package com.dore.customers;

/** Los totales se calculan de los pedidos y ventas; no se guardan. */
public record CustomerView(
        long id,
        String name,
        String phone,
        String email,
        String notes,
        long ordersCount,
        long totalSpent) {
}
