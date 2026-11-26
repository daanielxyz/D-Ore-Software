package com.dore.inventory;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class JpaInventoryService implements InventoryService {

    private final ProductRepository products;
    private final StockMovementRepository movements;

    JpaInventoryService(ProductRepository products, StockMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductView> search(String text, boolean includeInactive) {
        String term = text == null ? "" : text.strip().toLowerCase(Locale.ROOT);
        String escaped = term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return products.search("%" + escaped + "%", includeInactive).stream()
                .map(Product::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductView get(long productId) {
        return products.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId))
                .toView();
    }

    @Override
    public ProductView create(NewProduct data, MovementSource source) {
        String sku = normalizeSku(data.sku());
        if (sku == null) {
            sku = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } else {
            requireFreeSku(sku);
        }
        Product product = new Product(sku);
        product.describe(data.name(), data.category(), data.unit(), data.minStock(),
                data.unitCost(), data.unitPrice(), data.supplier());
        products.save(product);
        if (data.initialStock() > 0) {
            move(product, new NewMovement(MovementType.IN, data.initialStock(), "Stock inicial"), source);
        }
        return product.toView();
    }

    @Override
    public ProductView update(long productId, ProductChanges data) {
        Product product = lockActive(productId);
        String sku = normalizeSku(data.sku());
        if (sku != null && !sku.equalsIgnoreCase(product.toView().sku())) {
            requireFreeSku(sku);
            product.changeSku(sku);
        }
        product.describe(data.name(), data.category(), data.unit(), data.minStock(),
                data.unitCost(), data.unitPrice(), data.supplier());
        return product.toView();
    }

    @Override
    public void deactivate(long productId) {
        lockActive(productId).deactivate();
    }

    @Override
    public MovementView recordMovement(long productId, NewMovement movement, MovementSource source) {
        return move(lockActive(productId), movement, source);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementView> movements(long productId) {
        if (!products.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return movements.findByProductIdOrderByIdDesc(productId).stream()
                .map(StockMovement::toView)
                .toList();
    }

    private MovementView move(Product product, NewMovement movement, MovementSource source) {
        int delta = product.apply(movement.type(), movement.quantity());
        return movements.save(new StockMovement(product.getId(), movement.type(), delta,
                product.toView().stock(), movement.reason(), source)).toView();
    }

    private Product lockActive(long productId) {
        Product product = products.findForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!product.toView().active()) {
            throw new InventoryRuleException("El producto está desactivado.");
        }
        return product;
    }

    private void requireFreeSku(String sku) {
        if (products.existsBySkuIgnoreCase(sku)) {
            throw new InventoryRuleException("Ya existe un producto con el SKU " + sku + ".");
        }
    }

    private static String normalizeSku(String sku) {
        return sku == null || sku.isBlank() ? null : sku.strip().toUpperCase(Locale.ROOT);
    }
}
