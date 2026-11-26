package com.dore.assistantprototype.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
		@NotBlank(message = "El nombre es obligatorio.") String name,
		@NotBlank(message = "La categoría es obligatoria.") String category,
		@NotBlank(message = "La unidad de medida es obligatoria.") String unit,
		@NotBlank(message = "El SKU es obligatorio.") String sku,
		@NotNull(message = "El stock inicial es obligatorio.") @Min(value = 0, message = "El stock inicial no puede ser negativo.") Integer initialStock,
		@NotNull(message = "El stock mínimo es obligatorio.") @Min(value = 0, message = "El stock mínimo no puede ser negativo.") Integer minStock,
		@NotNull(message = "El costo es obligatorio.") @Min(value = 0, message = "El costo en pesos no puede ser negativo.") Integer cost,
		@NotNull(message = "El precio es obligatorio.") @Min(value = 0, message = "El precio en pesos no puede ser negativo.") Integer price,
		String supplier,
		String performedBy) {
}
