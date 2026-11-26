package com.dore.inventory;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_movement")
class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long productId;
    @Enumerated(EnumType.STRING)
    private MovementType type;
    private int delta;
    private int stockAfter;
    private String reason;
    @Enumerated(EnumType.STRING)
    private MovementSource source;
    private Instant createdAt;

    protected StockMovement() {
    }

    StockMovement(long productId, MovementType type, int delta, int stockAfter,
            String reason, MovementSource source) {
        this.productId = productId;
        this.type = type;
        this.delta = delta;
        this.stockAfter = stockAfter;
        this.reason = reason == null ? "" : reason.strip();
        this.source = source;
        this.createdAt = Instant.now();
    }

    MovementView toView() {
        return new MovementView(id, productId, type, delta, stockAfter, reason, source, createdAt);
    }
}
