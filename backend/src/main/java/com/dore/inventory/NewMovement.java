package com.dore.inventory;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Para IN, OUT y WASTE la cantidad debe ser mayor que cero.
 * Para ADJUSTMENT es el stock contado y puede ser cero.
 *
 * expiresOn: vencimiento de lo que entra (IN, o ADJUSTMENT que sube el stock).
 * lotId: lote del que sale (OUT o WASTE). Sin él sale primero lo que vence antes.
 */
public record NewMovement(
        @NotNull MovementType type,
        @Min(0) int quantity,
        @Size(max = 200) String reason,
        LocalDate expiresOn,
        Long lotId) {

    /** Movimiento sin fecha de vencimiento ni lote específico. */
    public NewMovement(MovementType type, int quantity, String reason) {
        this(type, quantity, reason, null, null);
    }
}
