package com.dore.inventory;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(long productId) {
        super("No existe el producto " + productId + ".");
    }
}
