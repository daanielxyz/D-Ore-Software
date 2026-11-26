package com.dore.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Para IN, OUT y WASTE la cantidad debe ser mayor que cero.
 * Para ADJUSTMENT es el stock contado y puede ser cero.
 */
public record NewMovement(
        @NotNull MovementType type,
        @Min(0) int quantity,
        @Size(max = 200) String reason) {
}
