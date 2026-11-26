package com.dore.inventory;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_lot")
class StockLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long productId;
    private int quantity;
    private LocalDate expiresOn;
    private Instant receivedAt;

    protected StockLot() {
    }

    StockLot(long productId, int quantity, LocalDate expiresOn) {
        this.productId = productId;
        this.quantity = quantity;
        this.expiresOn = expiresOn;
        this.receivedAt = Instant.now();
    }

    /** Saca hasta la cantidad pedida y devuelve cuánto sacó. */
    int take(int requested) {
        int taken = Math.min(requested, quantity);
        quantity -= taken;
        return taken;
    }

    /** Vence al terminar la fecha indicada. */
    boolean expiredOn(LocalDate today) {
        return expiresOn != null && expiresOn.isBefore(today);
    }

    Long getId() {
        return id;
    }

    long getProductId() {
        return productId;
    }

    int getQuantity() {
        return quantity;
    }

    LocalDate getExpiresOn() {
        return expiresOn;
    }

    LotView toView(LocalDate today) {
        return new LotView(id, productId, quantity, expiresOn, expiredOn(today), receivedAt);
    }
}
