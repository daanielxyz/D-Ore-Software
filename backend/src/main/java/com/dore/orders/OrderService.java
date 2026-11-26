package com.dore.orders;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dore.inventory.InventoryService;
import com.dore.inventory.MovementSource;
import com.dore.inventory.MovementType;
import com.dore.inventory.NewMovement;
import com.dore.inventory.ProductView;
import com.dore.sales.NewSale;
import com.dore.sales.SaleService;
import com.dore.shared.BusinessCalendar;
import com.dore.shared.NotFoundException;
import com.dore.shared.RuleException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El inventario se descuenta cuando el pedido pasa a "En diseño" (o a un estado
 * posterior). Al entregarlo se registra la venta.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orders;
    private final InventoryService inventory;
    private final SaleService sales;

    OrderService(OrderRepository orders, InventoryService inventory, SaleService sales) {
        this.orders = orders;
        this.inventory = inventory;
        this.sales = sales;
    }

    @Transactional(readOnly = true)
    public List<OrderView> list() {
        return orders.findAll();
    }

    public OrderView create(NewOrder order) {
        if (order.deliveryDate().isBefore(BusinessCalendar.today())) {
            throw new RuleException("La fecha de entrega no puede ser pasada.");
        }
        if (!orders.customerExists(order.customerId())) {
            throw new NotFoundException("No existe el cliente " + order.customerId() + ".");
        }
        // Une las líneas repetidas del mismo producto.
        Map<Long, Integer> lines = new TreeMap<>();
        for (NewOrder.Line line : order.items()) {
            lines.merge(line.productId(), line.quantity(), Math::addExact);
        }
        Map<Long, Long> prices = new TreeMap<>();
        long sum = 0;
        for (var line : lines.entrySet()) {
            ProductView product = inventory.get(line.getKey());
            if (!product.active()) {
                throw new RuleException(product.name() + " está desactivado.");
            }
            prices.put(product.id(), product.unitPrice());
            sum = Math.addExact(sum, Math.multiplyExact(product.unitPrice(), (long) line.getValue()));
        }
        long id = orders.insert(order, order.total() == null ? sum : order.total());
        lines.forEach((productId, quantity) ->
                orders.insertItem(id, productId, quantity, prices.get(productId)));
        return get(id);
    }

    /**
     * Solo permite avanzar o cancelar. Si el stock no alcanza, no cambia nada.
     * Cancelar un pedido ya armado no devuelve flores al inventario.
     */
    public OrderView changeStatus(long id, OrderStatus target) {
        if (!orders.lock(id)) {
            throw new NotFoundException("No existe el pedido " + id + ".");
        }
        OrderView order = get(id);
        OrderStatus current = order.status();
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new RuleException("El pedido " + order.code() + " ya está cerrado.");
        }
        if (target != OrderStatus.CANCELLED && target.ordinal() <= current.ordinal()) {
            throw new RuleException("Un pedido no puede devolverse de estado.");
        }
        boolean deducted = order.stockDeducted();
        if (!deducted && target != OrderStatus.CANCELLED) {
            // Siempre en el mismo orden de producto, para evitar bloqueos cruzados.
            for (OrderView.Item item : order.items()) {
                inventory.recordMovement(item.productId(),
                        new NewMovement(MovementType.OUT, item.quantity(), "Pedido " + order.code()),
                        MovementSource.MANUAL);
            }
            deducted = true;
        }
        if (target == OrderStatus.DELIVERED && order.total() > 0) {
            sales.registerForOrder(new NewSale(order.code() + " · " + order.occasion(), order.total(),
                    order.channel(), null, order.customerId(), null), id);
        }
        orders.updateStatus(id, target, deducted);
        return get(id);
    }

    private OrderView get(long id) {
        return orders.find(id)
                .orElseThrow(() -> new NotFoundException("No existe el pedido " + id + "."));
    }
}
