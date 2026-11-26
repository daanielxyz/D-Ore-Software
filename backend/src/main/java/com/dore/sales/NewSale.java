package com.dore.sales;

import java.time.LocalDate;
import java.util.List;

import com.dore.shared.SalesChannel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Los productos indicados se descuentan del inventario. Sin valor, se suma su
 * precio de venta. Sin fecha se toma el día de hoy en Cali.
 */
public record NewSale(
        @Size(max = 200) String description,
        @Min(1) Long amount,
        @NotNull SalesChannel channel,
        LocalDate saleDate,
        Long customerId,
        List<@Valid @NotNull Line> items) {

    public record Line(@NotNull Long productId, @Min(1) int quantity) {
    }
}
