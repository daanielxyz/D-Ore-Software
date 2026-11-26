package com.dore.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dore.inventory.InventoryService;
import com.dore.inventory.ProductView;
import com.dore.orders.OrderService;
import com.dore.sales.SaleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = "app.demo-data=true")
@Testcontainers
class DemoDataTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    InventoryService inventory;
    @Autowired
    OrderService orders;
    @Autowired
    SaleService sales;
    @Autowired
    DemoData demo;

    @Test
    void loadsTheCatalogTheExampleCommandsExpectAndOnlyOnce() {
        demo.run(null);

        assertThat(inventory.search("", true))
                .extracting(ProductView::sku, ProductView::stock)
                .containsExactlyInAnyOrder(tuple("FL-001", 84), tuple("FL-002", 48), tuple("FL-003", 42),
                        tuple("FL-004", 7), tuple("FL-005", 30), tuple("FO-001", 18), tuple("EM-001", 9),
                        tuple("EM-002", 26), tuple("IN-001", 15));
        assertThat(inventory.search("FL-005", false).getFirst().expiredStock()).isEqualTo(4);
        assertThat(orders.list()).hasSize(3);
        assertThat(sales.recent()).hasSize(4);
    }
}
