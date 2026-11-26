package com.dore.inventory;

import java.time.Instant;

public record ProductView(
        long id,
        String sku,
        String name,
        String category,
        Unit unit,
        int stock,
        int minStock,
        long unitCost,
        long unitPrice,
        String supplier,
        boolean active,
        boolean lowStock,
        Instant updatedAt) {
}
