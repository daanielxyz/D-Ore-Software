package com.dore.inventory;

import java.util.List;

/**
 * Contrato del inventario. La interfaz web y el asistente usan únicamente esto.
 * Todo cambio de stock queda registrado como un movimiento.
 */
public interface InventoryService {

    /** Busca por nombre, SKU, categoría o proveedor. Con texto vacío devuelve todo. */
    List<ProductView> search(String text, boolean includeInactive);

    ProductView get(long productId);

    /** Si el stock inicial es mayor que cero, registra un movimiento de entrada. */
    ProductView create(NewProduct product, MovementSource source);

    /** Cambia los datos del producto. El stock solo cambia con movimientos. */
    ProductView update(long productId, ProductChanges changes);

    /** El producto se oculta y conserva su historial. */
    void deactivate(long productId);

    MovementView recordMovement(long productId, NewMovement movement, MovementSource source);

    /** Del más reciente al más antiguo. */
    List<MovementView> movements(long productId);
}
