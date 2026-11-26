package com.dore.assistantprototype.inventory.repository;

import com.dore.assistantprototype.inventory.domain.MovementOrigin;
import com.dore.assistantprototype.inventory.domain.MovementType;
import com.dore.assistantprototype.inventory.domain.StockMovement;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryStockMovementRepository implements StockMovementRepository {

	private final List<StockMovement> ledger = new CopyOnWriteArrayList<>();
	private final AtomicLong idGenerator = new AtomicLong(0);

	@Override
	public synchronized StockMovement append(
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
		long id = idGenerator.incrementAndGet();
		StockMovement entry = new StockMovement(
				id,
				productId,
				productSku,
				productName,
				type,
				quantity,
				previousStock,
				resultingStock,
				reason,
				origin,
				performedBy,
				createdAt);
		ledger.add(entry);
		return entry;
	}

	@Override
	public List<StockMovement> findByProductId(Long productId) {
		return ledger.stream()
				.filter(item -> item.productId().equals(productId))
				.sorted(Comparator.comparing(StockMovement::id).reversed())
				.toList();
	}

	@Override
	public List<StockMovement> findAll() {
		return ledger.stream()
				.sorted(Comparator.comparing(StockMovement::id).reversed())
				.toList();
	}

	@Override
	public synchronized void clear() {
		ledger.clear();
		idGenerator.set(0);
	}
}
