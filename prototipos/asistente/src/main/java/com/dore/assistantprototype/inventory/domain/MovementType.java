package com.dore.assistantprototype.inventory.domain;

import java.util.Locale;

public enum MovementType {
	ENTRY("entrada"),
	EXIT("salida"),
	WASTE("merma"),
	ADJUSTMENT("ajuste");

	private final String spanishLabel;

	MovementType(String spanishLabel) {
		this.spanishLabel = spanishLabel;
	}

	public String spanishLabel() {
		return spanishLabel;
	}

	public static MovementType fromFlexibleString(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
		}
		String normalized = raw.trim().toUpperCase(Locale.ROOT);
		return switch (normalized) {
			case "ENTRY", "ENTRADA" -> ENTRY;
			case "EXIT", "SALIDA" -> EXIT;
			case "WASTE", "MERMA" -> WASTE;
			case "ADJUSTMENT", "AJUSTE" -> ADJUSTMENT;
			default -> throw new IllegalArgumentException("Tipo de movimiento no válido: " + raw);
		};
	}
}
