package com.dore.orders;

import java.time.LocalDate;
import java.util.List;

import com.dore.inventory.Unit;
import com.dore.shared.SalesChannel;

public record OrderView(
        long id,
        String code,
        long customerId,
        String customerName,
        String occasion,
        SalesChannel channel,
        LocalDate deliveryDate,
        OrderStatus status,
        long total,
        String notes,
        boolean stockDeducted,
        List<Item> items) {

    public record Item(long productId, String productName, Unit unit, int quantity, long unitPrice) {
    }
}
