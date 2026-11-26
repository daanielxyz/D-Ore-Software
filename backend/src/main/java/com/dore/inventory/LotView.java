package com.dore.inventory;

import java.time.Instant;
import java.time.LocalDate;

/** expiresOn es nulo cuando el lote no vence. */
public record LotView(
        long id,
        long productId,
        int quantity,
        LocalDate expiresOn,
        boolean expired,
        Instant receivedAt) {
}
