package com.dore.sales;

import java.time.LocalDate;

import com.dore.shared.SalesChannel;

public record SaleView(
        long id,
        String description,
        long amount,
        SalesChannel channel,
        LocalDate saleDate,
        Long customerId,
        String customerName,
        Long orderId) {
}
