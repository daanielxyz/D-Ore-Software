package com.dore.assistantprototype.inventory.exception;

public class InsufficientStockException extends RuntimeException {

	private final int availableStock;
	private final int requestedQuantity;

	public InsufficientStockException(String productName, int availableStock, int requestedQuantity) {
		super("Stock insuficiente para '" + productName + "': hay " + availableStock
				+ " disponibles y se solicitaron " + requestedQuantity + ".");
		this.availableStock = availableStock;
		this.requestedQuantity = requestedQuantity;
	}

	public int getAvailableStock() {
		return availableStock;
	}

	public int getRequestedQuantity() {
		return requestedQuantity;
	}
}
