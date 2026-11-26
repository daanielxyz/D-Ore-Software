package com.dore.assistantprototype.inventory.dto;

import com.dore.assistantprototype.inventory.domain.Product;

public record ProductResponse(
		Long id,
		String name,
		String category,
		String unit,
		String sku,
		int stock,
		int minStock,
		int cost,
		int price,
		String supplier,
		boolean active,
		boolean lowStock) {

	public static ProductResponse fromDomain(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getCategory(),
				product.getUnit(),
				product.getSku(),
				product.getStock(),
				product.getMinStock(),
				product.getCost(),
				product.getPrice(),
				product.getSupplier(),
				product.isActive(),
				product.isActive() && product.getStock() <= product.getMinStock());
	}
}
