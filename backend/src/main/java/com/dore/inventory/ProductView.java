package com.dore.inventory;

import java.time.Instant;
import java.time.LocalDate;

/**
 * stock incluye lo vencido que aún no se ha dado de baja; expiredStock dice cuánto.
 * nextExpiry es el vencimiento más próximo entre lo que no ha vencido.
 */
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
        Instant updatedAt,
        int expiredStock,
        LocalDate nextExpiry) {
}
