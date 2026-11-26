package com.dore.inventory;

import java.time.Instant;

/** delta es el cambio aplicado (negativo si restó). */
public record MovementView(
        long id,
        long productId,
        MovementType type,
        int delta,
        int stockAfter,
        String reason,
        MovementSource source,
        Instant createdAt) {
}
