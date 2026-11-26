package com.dore.sales;

import java.time.LocalDate;
import java.util.List;

import com.dore.shared.BusinessCalendar;
import com.dore.shared.NotFoundException;
import com.dore.shared.RuleException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaleService {

    private final SaleRepository sales;

    SaleService(SaleRepository sales) {
        this.sales = sales;
    }

    public List<SaleView> recent() {
        return sales.findRecent();
    }

    /** Venta de mostrador. No descuenta inventario. */
    public void register(NewSale sale) {
        save(sale, null);
    }

    /** La venta de un pedido entregado. */
    public void registerForOrder(NewSale sale, long orderId) {
        save(sale, orderId);
    }

    private void save(NewSale sale, Long orderId) {
        LocalDate date = sale.saleDate() == null ? BusinessCalendar.today() : sale.saleDate();
        if (date.isAfter(BusinessCalendar.today())) {
            throw new RuleException("La fecha de la venta no puede ser futura.");
        }
        if (sale.customerId() != null && !sales.customerExists(sale.customerId())) {
            throw new NotFoundException("No existe el cliente " + sale.customerId() + ".");
        }
        sales.insert(sale, date, orderId);
    }
}
