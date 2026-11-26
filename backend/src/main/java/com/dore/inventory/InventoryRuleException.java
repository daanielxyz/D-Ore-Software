package com.dore.inventory;

/** Una operación que las reglas del inventario no permiten. */
public class InventoryRuleException extends RuntimeException {
    public InventoryRuleException(String message) {
        super(message);
    }
}
