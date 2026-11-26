package com.dore.orders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dore.inventory.Unit;
import com.dore.shared.SalesChannel;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class OrderRepository {

    private record Row(long id, long customerId, String customerName, String occasion,
            SalesChannel channel, LocalDate deliveryDate, OrderStatus status, long total,
            String notes, boolean stockDeducted) {
    }

    private record ItemRow(long orderId, long productId, String productName, Unit unit,
            int quantity, long unitPrice) {
    }

    private static final String SELECT = """
            select o.id, o.customer_id, c.name as customer_name, o.occasion, o.channel,
                   o.delivery_date, o.status, o.total, o.notes, o.stock_deducted
            from customer_order o join customer c on c.id = o.customer_id
            """;

    private final JdbcClient jdbc;

    OrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<OrderView> findAll() {
        return withItems(jdbc.sql(SELECT + " order by o.delivery_date desc, o.id desc limit 200")
                .query(Row.class).list());
    }

    Optional<OrderView> find(long id) {
        return withItems(jdbc.sql(SELECT + " where o.id = ?").param(id).query(Row.class).list())
                .stream().findFirst();
    }

    /** Bloquea el pedido para que dos cambios simultáneos no descuenten dos veces. */
    boolean lock(long id) {
        return jdbc.sql("select id from customer_order where id = ? for update").param(id)
                .query(Long.class).optional().isPresent();
    }

    boolean customerExists(long id) {
        return jdbc.sql("select count(*) from customer where id = ?").param(id)
                .query(Integer.class).single() > 0;
    }

    long insert(NewOrder order, long total) {
        return jdbc.sql("""
                insert into customer_order
                  (customer_id, occasion, channel, delivery_date, total, notes)
                values (?,?,?,?,?,?) returning id
                """)
                .params(order.customerId(), order.occasion().strip(), order.channel().name(),
                        order.deliveryDate(), total,
                        order.notes() == null ? "" : order.notes().strip())
                .query(Long.class).single();
    }

    void insertItem(long orderId, long productId, int quantity, long unitPrice) {
        jdbc.sql("insert into order_item (order_id, product_id, quantity, unit_price) values (?,?,?,?)")
                .params(orderId, productId, quantity, unitPrice)
                .update();
    }

    void updateStatus(long id, OrderStatus status, boolean stockDeducted) {
        jdbc.sql("update customer_order set status = ?, stock_deducted = ? where id = ?")
                .params(status.name(), stockDeducted, id)
                .update();
    }

    private List<OrderView> withItems(List<Row> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ItemRow>> items = jdbc.sql("""
                select i.order_id, i.product_id, p.name as product_name, p.unit,
                       i.quantity, i.unit_price
                from order_item i join product p on p.id = i.product_id
                where i.order_id in (:ids)
                order by i.product_id
                """)
                .param("ids", rows.stream().map(Row::id).toList())
                .query(ItemRow.class).list().stream()
                .collect(Collectors.groupingBy(ItemRow::orderId));
        return rows.stream()
                .map(r -> new OrderView(r.id(), String.format("FL-%05d", r.id()), r.customerId(),
                        r.customerName(), r.occasion(), r.channel(), r.deliveryDate(), r.status(),
                        r.total(), r.notes(), r.stockDeducted(),
                        items.getOrDefault(r.id(), List.of()).stream()
                                .map(i -> new OrderView.Item(i.productId(), i.productName(),
                                        i.unit(), i.quantity(), i.unitPrice()))
                                .toList()))
                .toList();
    }
}
