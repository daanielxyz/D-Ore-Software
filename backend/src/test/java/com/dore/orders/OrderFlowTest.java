package com.dore.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import com.dore.customers.CustomerData;
import com.dore.customers.CustomerService;
import com.dore.inventory.InventoryRuleException;
import com.dore.inventory.InventoryService;
import com.dore.inventory.MovementSource;
import com.dore.inventory.NewProduct;
import com.dore.inventory.Unit;
import com.dore.sales.NewSale;
import com.dore.sales.SaleService;
import com.dore.shared.BusinessCalendar;
import com.dore.shared.RuleException;
import com.dore.shared.SalesChannel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class OrderFlowTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    OrderService orders;
    @Autowired
    InventoryService inventory;
    @Autowired
    CustomerService customers;
    @Autowired
    SaleService sales;

    private long product(String name, int stock) {
        return inventory.create(new NewProduct(name, "Flores", Unit.STEM, null, 0, 1000, 3000, "",
                stock, null), MovementSource.MANUAL).id();
    }

    private long customer() {
        return customers.create(new CustomerData("Cliente de prueba", "", "", "")).id();
    }

    private OrderView order(long customerId, NewOrder.Line... lines) {
        return orders.create(new NewOrder(customerId, "Cumpleaños", SalesChannel.WHATSAPP,
                BusinessCalendar.today(), null, "", List.of(lines)));
    }

    private int stock(long productId) {
        return inventory.get(productId).stock();
    }

    @Test
    void stockIsDeductedOnceAndTheSaleIsRecordedOnDelivery() {
        long rose = product("Rosa pedido", 20);
        long client = customer();
        OrderView created = order(client, new NewOrder.Line(rose, 4), new NewOrder.Line(rose, 2));

        assertThat(created.total()).isEqualTo(18_000);
        assertThat(created.items()).singleElement().satisfies(i -> assertThat(i.quantity()).isEqualTo(6));
        assertThat(stock(rose)).isEqualTo(20);

        orders.changeStatus(created.id(), OrderStatus.IN_DESIGN);
        assertThat(stock(rose)).isEqualTo(14);
        orders.changeStatus(created.id(), OrderStatus.READY);
        assertThat(stock(rose)).isEqualTo(14);
        assertThat(customers.get(client).totalSpent()).isZero();

        orders.changeStatus(created.id(), OrderStatus.DELIVERED);

        assertThat(stock(rose)).isEqualTo(14);
        assertThat(customers.get(client).totalSpent()).isEqualTo(18_000);
        assertThat(sales.recent()).filteredOn(s -> Long.valueOf(created.id()).equals(s.orderId())).hasSize(1);
        assertThatThrownBy(() -> orders.changeStatus(created.id(), OrderStatus.CANCELLED))
                .isInstanceOf(RuleException.class);
    }

    @Test
    void counterSalesDeductStockAllOrNothing() {
        long rose = product("Rosa mostrador", 10);
        long scarce = product("Escaso mostrador", 1);

        sales.register(new NewSale(null, null, SalesChannel.STORE, null, null,
                List.of(new NewSale.Line(rose, 3))));

        assertThat(stock(rose)).isEqualTo(7);
        assertThat(sales.recent()).anySatisfy(s -> {
            assertThat(s.description()).isEqualTo("3 × Rosa mostrador");
            assertThat(s.amount()).isEqualTo(9_000);
        });

        int before = sales.recent().size();
        assertThatThrownBy(() -> sales.register(new NewSale("x", 5_000L, SalesChannel.STORE, null, null,
                List.of(new NewSale.Line(rose, 2), new NewSale.Line(scarce, 2)))))
                .isInstanceOf(InventoryRuleException.class);
        assertThat(stock(rose)).isEqualTo(7);
        assertThat(sales.recent()).hasSize(before);
        assertThatThrownBy(() -> sales.register(
                new NewSale("sin valor", null, SalesChannel.STORE, null, null, List.of())))
                .isInstanceOf(RuleException.class);
    }

    @Test
    void nothingChangesWhenOneProductIsShort() {
        long enough = product("Hay suficiente", 10);
        long scarce = product("No alcanza", 1);
        OrderView created = order(customer(), new NewOrder.Line(enough, 5), new NewOrder.Line(scarce, 3));

        assertThatThrownBy(() -> orders.changeStatus(created.id(), OrderStatus.IN_DESIGN))
                .isInstanceOf(InventoryRuleException.class);

        assertThat(stock(enough)).isEqualTo(10);
        assertThat(stock(scarce)).isEqualTo(1);
        assertThat(inventory.movements(enough)).hasSize(1);
        assertThat(orders.list()).filteredOn(o -> o.id() == created.id())
                .singleElement()
                .satisfies(o -> assertThat(o.status()).isEqualTo(OrderStatus.PENDING));
    }

    @Test
    void cancellingAPendingOrderLeavesStockAloneAndStatusNeverGoesBack() {
        long lily = product("Lirio pedido", 8);
        long client = customer();
        OrderView pending = order(client, new NewOrder.Line(lily, 2));
        orders.changeStatus(pending.id(), OrderStatus.CANCELLED);
        assertThat(stock(lily)).isEqualTo(8);

        OrderView advanced = order(client, new NewOrder.Line(lily, 2));
        orders.changeStatus(advanced.id(), OrderStatus.READY);
        assertThat(stock(lily)).isEqualTo(6);
        assertThatThrownBy(() -> orders.changeStatus(advanced.id(), OrderStatus.IN_DESIGN))
                .isInstanceOf(RuleException.class);
    }

    @Test
    void simultaneousStatusChangesDeductOnlyOnce() throws Exception {
        long tulip = product("Tulipan pedido", 50);
        OrderView created = order(customer(), new NewOrder.Line(tulip, 5));
        List<Callable<OrderView>> attempts = IntStream.range(0, 6)
                .<Callable<OrderView>>mapToObj(i -> () -> orders.changeStatus(created.id(), OrderStatus.IN_DESIGN))
                .toList();

        int succeeded = 0;
        try (var pool = Executors.newFixedThreadPool(6)) {
            for (var result : pool.invokeAll(attempts)) {
                try {
                    result.get();
                    succeeded++;
                } catch (ExecutionException rejected) {
                    assertThat(rejected.getCause()).isInstanceOf(RuleException.class);
                }
            }
        }

        assertThat(succeeded).isEqualTo(1);
        assertThat(stock(tulip)).isEqualTo(45);
    }
}
