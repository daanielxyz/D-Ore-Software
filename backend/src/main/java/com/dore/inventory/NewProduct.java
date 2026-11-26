package com.dore.inventory;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dinero en pesos enteros. Si no se indica SKU, se genera uno. */
public record NewProduct(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 60) String category,
        @NotNull Unit unit,
        @Size(max = 40) String sku,
        @Min(0) int minStock,
        @Min(0) long unitCost,
        @Min(0) long unitPrice,
        @Size(max = 120) String supplier,
        @Min(0) int initialStock,
        LocalDate initialExpiresOn) {
}
