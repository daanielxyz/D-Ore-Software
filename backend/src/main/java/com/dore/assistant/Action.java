package com.dore.assistant;

import com.dore.inventory.MovementType;
import com.dore.inventory.NewMovement;
import com.dore.inventory.NewProduct;
import com.dore.inventory.ProductChanges;
import com.dore.inventory.Unit;

/** Un cambio ya validado que espera confirmación. */
sealed interface Action {

    record Move(long productId, NewMovement movement) implements Action {
    }

    record Create(NewProduct product) implements Action {
    }

    /** Solo los campos no nulos cambian; el resto se toma del producto al confirmar. */
    record Update(long productId, String name, String category, Unit unit, Integer minStock,
            Long unitCost, Long unitPrice, String supplier) implements Action {

        ProductChanges applyTo(com.dore.inventory.ProductView current) {
            return new ProductChanges(
                    name != null ? name : current.name(),
                    category != null ? category : current.category(),
                    unit != null ? unit : current.unit(),
                    current.sku(),
                    minStock != null ? minStock : current.minStock(),
                    unitCost != null ? unitCost : current.unitCost(),
                    unitPrice != null ? unitPrice : current.unitPrice(),
                    supplier != null ? supplier : current.supplier());
        }
    }

    record Deactivate(long productId) implements Action {
    }

    /** Lo que se muestra al usuario antes de confirmar. */
    record Preview(String kind, String sku, MovementType movementType, Integer quantity, String summary) {
    }
}
