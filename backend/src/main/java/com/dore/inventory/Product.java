package com.dore.inventory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sku;
    private String name;
    private String category;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private int stock;
    private int minStock;
    private long unitCost;
    private long unitPrice;
    private String supplier;
    private boolean active = true;
    private Instant updatedAt = Instant.now();

    protected Product() {
    }

    Product(String sku) {
        this.sku = sku;
    }

    void describe(String name, String category, Unit unit, int minStock,
            long unitCost, long unitPrice, String supplier) {
        this.name = name.strip();
        this.category = category.strip();
        this.unit = unit;
        this.minStock = minStock;
        this.unitCost = unitCost;
        this.unitPrice = unitPrice;
        this.supplier = supplier == null ? "" : supplier.strip();
        touch();
    }

    void changeSku(String sku) {
        this.sku = sku;
        touch();
    }

    /** Aplica el movimiento y devuelve el cambio de stock resultante. */
    int apply(MovementType type, int quantity) {
        if (quantity < 0 || (quantity == 0 && type != MovementType.ADJUSTMENT)) {
            throw new InventoryRuleException("La cantidad debe ser mayor que cero.");
        }
        long result = switch (type) {
            case IN -> (long) stock + quantity;
            case OUT, WASTE -> (long) stock - quantity;
            case ADJUSTMENT -> quantity;
        };
        if (result < 0) {
            throw new InventoryRuleException(
                    "No hay suficiente stock de " + name + ": quedan " + stock + ".");
        }
        if (result > Integer.MAX_VALUE) {
            throw new InventoryRuleException("La cantidad es demasiado grande.");
        }
        int delta = (int) result - stock;
        stock = (int) result;
        touch();
        return delta;
    }

    void deactivate() {
        active = false;
        touch();
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    /** El stock bajo se calcula sin contar lo vencido. */
    ProductView toView(List<StockLot> lots, LocalDate today) {
        int expired = lots.stream().filter(l -> l.expiredOn(today)).mapToInt(StockLot::getQuantity).sum();
        LocalDate next = lots.stream()
                .filter(l -> l.getQuantity() > 0 && !l.expiredOn(today))
                .map(StockLot::getExpiresOn)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new ProductView(id, sku, name, category, unit, stock, minStock, unitCost,
                unitPrice, supplier, active, stock - expired <= minStock, updatedAt, expired, next);
    }

    String getName() {
        return name;
    }

    String getSku() {
        return sku;
    }

    int getStock() {
        return stock;
    }

    boolean isActive() {
        return active;
    }

    Long getId() {
        return id;
    }
}
