package com.dore.sales;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.dore.inventory.InventoryService;
import com.dore.inventory.MovementSource;
import com.dore.inventory.MovementType;
import com.dore.inventory.NewMovement;
import com.dore.inventory.ProductView;
import com.dore.shared.BusinessCalendar;
import com.dore.shared.NotFoundException;
import com.dore.shared.RuleException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaleService {

    private final SaleRepository sales;
    private final InventoryService inventory;

    SaleService(SaleRepository sales, InventoryService inventory) {
        this.sales = sales;
        this.inventory = inventory;
    }

    public List<SaleView> recent() {
        return sales.findRecent();
    }

    /** Venta de mostrador: descuenta del inventario los productos indicados. */
    public void register(NewSale sale) {
        LocalDate date = validDate(sale);
        // Une líneas repetidas y las ordena para evitar bloqueos cruzados.
        Map<Long, Integer> lines = new TreeMap<>();
        if (sale.items() != null) {
            sale.items().forEach(l -> lines.merge(l.productId(), l.quantity(), Math::addExact));
        }
        Map<Long, ProductView> products = new TreeMap<>();
        long sum = 0;
        for (var line : lines.entrySet()) {
            ProductView product = inventory.get(line.getKey());
            products.put(product.id(), product);
            sum = Math.addExact(sum, Math.multiplyExact(product.unitPrice(), (long) line.getValue()));
        }
        long amount = sale.amount() == null ? sum : sale.amount();
        if (amount <= 0) {
            throw new RuleException("Indica el valor de la venta.");
        }
        String description = describe(sale, lines, products);
        long id = sales.insert(description, amount, sale, date, null);
        lines.forEach((productId, quantity) -> {
            inventory.recordMovement(productId,
                    new NewMovement(MovementType.OUT, quantity, "Venta de mostrador"),
                    MovementSource.MANUAL);
            sales.insertItem(id, productId, quantity, products.get(productId).unitPrice());
        });
    }

    /** La venta de un pedido entregado. Su inventario ya se descontó al armarlo. */
    public void registerForOrder(NewSale sale, long orderId) {
        sales.insert(sale.description(), sale.amount(), sale, validDate(sale), orderId);
    }

    private LocalDate validDate(NewSale sale) {
        LocalDate date = sale.saleDate() == null ? BusinessCalendar.today() : sale.saleDate();
        if (date.isAfter(BusinessCalendar.today())) {
            throw new RuleException("La fecha de la venta no puede ser futura.");
        }
        if (sale.customerId() != null && !sales.customerExists(sale.customerId())) {
            throw new NotFoundException("No existe el cliente " + sale.customerId() + ".");
        }
        return date;
    }

    private static String describe(NewSale sale, Map<Long, Integer> lines, Map<Long, ProductView> products) {
        if (sale.description() != null && !sale.description().isBlank()) {
            return sale.description().strip();
        }
        if (lines.isEmpty()) {
            throw new RuleException("Escribe un concepto o elige productos.");
        }
        String text = lines.entrySet().stream()
                .map(l -> l.getValue() + " × " + products.get(l.getKey()).name())
                .collect(Collectors.joining(", "));
        return text.length() > 200 ? text.substring(0, 199) + "…" : text;
    }
}
