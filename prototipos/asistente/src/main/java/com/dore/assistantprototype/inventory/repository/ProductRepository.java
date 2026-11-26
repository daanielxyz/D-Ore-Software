package com.dore.assistantprototype.inventory.repository;

import com.dore.assistantprototype.inventory.domain.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

	List<Product> findAll();

	Optional<Product> findById(Long id);

	Optional<Product> findBySkuIgnoreCase(String sku);

	Product saveNew(
			String name,
			String category,
			String unit,
			String sku,
			int stock,
			int minStock,
			int cost,
			int price,
			String supplier,
			boolean active);

	Product update(Product product);

	void clear();
}
