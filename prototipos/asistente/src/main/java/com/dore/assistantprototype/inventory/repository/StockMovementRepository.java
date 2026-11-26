package com.dore.assistantprototype.inventory.repository;

import com.dore.assistantprototype.inventory.domain.MovementOrigin;
import com.dore.assistantprototype.inventory.domain.MovementType;
import com.dore.assistantprototype.inventory.domain.StockMovement;
import java.time.ZonedDateTime;
import java.util.List;

public interface StockMovementRepository {

	StockMovement append(
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
			ZonedDateTime createdAt);

	List<StockMovement> findByProductId(Long productId);

	List<StockMovement> findAll();

	void clear();
}
