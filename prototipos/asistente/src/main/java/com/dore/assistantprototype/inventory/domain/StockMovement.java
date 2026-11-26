package com.dore.assistantprototype.inventory.domain;

import java.time.ZonedDateTime;

public record StockMovement(
		Long id,
		Long productId,
		String productSku,
		String productName,
		MovementType type,
		int quantity,
		int previousStock,
		int resultingStock,
		String reason,
		MovementOrigin origin,
		String performedBy,
		ZonedDateTime createdAt) {
}
