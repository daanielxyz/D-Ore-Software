package com.dore.inventory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class JpaInventoryService implements InventoryService {

    private final ProductRepository products;
    private final StockMovementRepository movements;

    private final StockLotRepository lots;

    /** El día del negocio es el de Cali, no el del servidor. */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");

    JpaInventoryService(ProductRepository products, StockMovementRepository movements,
            StockLotRepository lots) {
        this.products = products;
        this.movements = movements;
        this.lots = lots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductView> search(String text, boolean includeInactive) {
        String term = text == null ? "" : text.strip().toLowerCase(Locale.ROOT);
        String escaped = term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        List<Product> found = products.search("%" + escaped + "%", includeInactive);
        Map<Long, List<StockLot>> byProduct = lots
                .findAvailable(found.stream().map(Product::getId).toList()).stream()
                .collect(Collectors.groupingBy(StockLot::getProductId));
        LocalDate today = today();
        return found.stream()
                .map(p -> p.toView(byProduct.getOrDefault(p.getId(), List.of()), today))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductView get(long productId) {
        return view(products.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId)));
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
            move(product, new NewMovement(MovementType.IN, data.initialStock(), "Stock inicial",
                    data.initialExpiresOn(), null), source);
        }
        return view(product);
    }

    @Override
    public ProductView update(long productId, ProductChanges data) {
        Product product = lockActive(productId);
        String sku = normalizeSku(data.sku());
        if (sku != null && !sku.equalsIgnoreCase(product.getSku())) {
            requireFreeSku(sku);
            product.changeSku(sku);
        }
        product.describe(data.name(), data.category(), data.unit(), data.minStock(),
                data.unitCost(), data.unitPrice(), data.supplier());
        return view(product);
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

    @Override
    @Transactional(readOnly = true)
    public List<LotView> lots(long productId) {
        if (!products.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        LocalDate today = today();
        return lots.findAvailable(List.of(productId)).stream()
                .map(l -> l.toView(today))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LotView> expiringLots(int days) {
        if (days < 0 || days > 365) {
            throw new InventoryRuleException("Los días deben estar entre 0 y 365.");
        }
        LocalDate today = today();
        return lots.findExpiringUpTo(today.plusDays(days)).stream()
                .map(l -> l.toView(today))
                .toList();
    }

    /** Siempre con la fila del producto bloqueada o recién creada. */
    private MovementView move(Product product, NewMovement movement, MovementSource source) {
        MovementType type = movement.type();
        boolean takesFromLot = type == MovementType.OUT || type == MovementType.WASTE;
        if (movement.lotId() != null && !takesFromLot) {
            throw new InventoryRuleException("Solo las salidas y las mermas indican un lote.");
        }
        if (movement.expiresOn() != null && takesFromLot) {
            throw new InventoryRuleException("El vencimiento solo se indica en lo que entra.");
        }
        int delta = product.apply(type, movement.quantity());
        if (delta > 0) {
            if (movement.expiresOn() != null && movement.expiresOn().isBefore(today())) {
                throw new InventoryRuleException("No se puede ingresar flor ya vencida.");
            }
            lots.save(new StockLot(product.getId(), delta, movement.expiresOn()));
        } else if (delta < 0) {
            takeFromLots(product, type, -delta, movement.lotId());
        }
        return movements.save(new StockMovement(product.getId(), type, delta,
                product.getStock(), movement.reason(), source)).toView();
    }

    /** Si no alcanza, la excepción deshace toda la operación. */
    private void takeFromLots(Product product, MovementType type, int quantity, Long lotId) {
        LocalDate today = today();
        List<StockLot> candidates = lots.findAvailable(List.of(product.getId())).stream()
                .filter(l -> lotId == null || lotId.equals(l.getId()))
                .filter(l -> type != MovementType.OUT || !l.expiredOn(today))
                .toList();
        int pending = quantity;
        for (StockLot lot : candidates) {
            pending -= lot.take(pending);
        }
        if (pending > 0) {
            String detail = type == MovementType.OUT
                    ? " sin vencer. Lo vencido se registra como merma."
                    : " en el lote indicado.";
            throw new InventoryRuleException("De " + product.getName() + " solo hay "
                    + (quantity - pending) + detail);
        }
    }

    private ProductView view(Product product) {
        return product.toView(lots.findAvailable(List.of(product.getId())), today());
    }

    private static LocalDate today() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    private Product lockActive(long productId) {
        Product product = products.findForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!product.isActive()) {
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
