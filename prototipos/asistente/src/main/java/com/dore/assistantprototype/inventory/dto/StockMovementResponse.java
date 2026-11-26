package com.dore.assistantprototype.inventory.dto;

import com.dore.assistantprototype.inventory.domain.MovementOrigin;
import com.dore.assistantprototype.inventory.domain.MovementType;
import com.dore.assistantprototype.inventory.domain.StockMovement;
import java.time.ZonedDateTime;

public record StockMovementResponse(
		Long id,
		Long productId,
		String productSku,
		String productName,
		MovementType type,
		String typeLabel,
		int quantity,
		int previousStock,
		int resultingStock,
		String reason,
		MovementOrigin origin,
		String originLabel,
		String performedBy,
		ZonedDateTime createdAt) {

	public static StockMovementResponse fromDomain(StockMovement movement) {
		return new StockMovementResponse(
				movement.id(),
				movement.productId(),
				movement.productSku(),
				movement.productName(),
				movement.type(),
				movement.type().spanishLabel(),
				movement.quantity(),
				movement.previousStock(),
				movement.resultingStock(),
				movement.reason(),
				movement.origin(),
				movement.origin().spanishLabel(),
				movement.performedBy(),
				movement.createdAt());
	}
}
