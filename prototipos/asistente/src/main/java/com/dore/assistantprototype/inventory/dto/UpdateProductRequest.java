package com.dore.assistantprototype.inventory.dto;

import jakarta.validation.constraints.Min;

public record UpdateProductRequest(
		String name,
		String category,
		String unit,
		String sku,
		@Min(value = 0, message = "El stock mínimo no puede ser negativo.") Integer minStock,
		@Min(value = 0, message = "El costo en pesos no puede ser negativo.") Integer cost,
		@Min(value = 0, message = "El precio en pesos no puede ser negativo.") Integer price,
		String supplier) {
}
