package com.dore.assistantprototype.inventory.domain;

public enum MovementOrigin {
	MANUAL("manual"),
	ASSISTANT("asistente");

	private final String spanishLabel;

	MovementOrigin(String spanishLabel) {
		this.spanishLabel = spanishLabel;
	}

	public String spanishLabel() {
		return spanishLabel;
	}
}
