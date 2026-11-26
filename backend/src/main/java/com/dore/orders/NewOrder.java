package com.dore.orders;

import java.time.LocalDate;
import java.util.List;

import com.dore.shared.SalesChannel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Sin total, se suma el precio de venta de los productos. */
public record NewOrder(
        @NotNull Long customerId,
        @NotBlank @Size(max = 60) String occasion,
        @NotNull SalesChannel channel,
        @NotNull LocalDate deliveryDate,
        @Min(0) Long total,
        @Size(max = 300) String notes,
        @NotEmpty List<@Valid @NotNull Line> items) {

    public record Line(@NotNull Long productId, @Min(1) int quantity) {
    }
}
