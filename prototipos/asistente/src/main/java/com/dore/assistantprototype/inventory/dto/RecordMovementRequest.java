package com.dore.assistantprototype.inventory.dto;

import com.dore.assistantprototype.inventory.domain.MovementType;
import jakarta.validation.constraints.NotNull;

public record RecordMovementRequest(
		@NotNull(message = "El tipo de movimiento es obligatorio.") MovementType type,
		@NotNull(message = "La cantidad es obligatoria.") Integer quantity,
		String reason,
		String performedBy) {
}
