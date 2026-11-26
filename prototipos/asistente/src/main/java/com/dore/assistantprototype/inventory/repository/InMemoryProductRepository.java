package com.dore.assistantprototype.inventory.repository;

import com.dore.assistantprototype.inventory.domain.Product;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryProductRepository implements ProductRepository {

	private final Map<Long, Product> store = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(0);

	@Override
	public List<Product> findAll() {
		return store.values().stream()
				.sorted(Comparator.comparing(Product::getId))
				.map(Product::copy)
				.toList();
	}

	@Override
	public Optional<Product> findById(Long id) {
		return Optional.ofNullable(store.get(id)).map(Product::copy);
	}

	@Override
	public Optional<Product> findBySkuIgnoreCase(String sku) {
		if (sku == null) {
			return Optional.empty();
		}
		String trimmed = sku.trim();
		return store.values().stream()
				.filter(item -> item.getSku().equalsIgnoreCase(trimmed))
				.findFirst()
				.map(Product::copy);
	}

	@Override
	public synchronized Product saveNew(
			String name,
			String category,
			String unit,
			String sku,
			int stock,
			int minStock,
			int cost,
			int price,
			String supplier,
			boolean active) {
		long nextId = idGenerator.incrementAndGet();
		Product created = new Product(nextId, name, category, unit, sku, stock, minStock, cost, price, supplier, active);
		store.put(nextId, created.copy());
		return created;
	}

	@Override
	public synchronized Product update(Product product) {
		store.put(product.getId(), product.copy());
		return product.copy();
	}

	@Override
	public synchronized void clear() {
		store.clear();
		idGenerator.set(0);
	}
}
