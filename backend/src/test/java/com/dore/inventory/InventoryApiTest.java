package com.dore.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Testcontainers
class InventoryApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    InventoryService inventory;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private ProductView create(String name, int stock) {
        return inventory.create(new NewProduct(name, "Flores", Unit.STEM, null, 5, 1800, 4200,
                "Proveedor de prueba", stock, null), MovementSource.MANUAL);
    }

    private static NewMovement movement(MovementType type, int quantity) {
        return new NewMovement(type, quantity, "prueba");
    }

    @Test
    void initialStockIsRecordedAsAMovement() {
        ProductView product = create("Rosa", 20);

        assertThat(product.stock()).isEqualTo(20);
        assertThat(inventory.movements(product.id()))
                .singleElement()
                .satisfies(m -> assertThat(m.delta()).isEqualTo(20));
    }

    @Test
    void movementsChangeStockAndKeepHistory() {
        long id = create("Girasol", 10).id();

        inventory.recordMovement(id, movement(MovementType.IN, 5), MovementSource.ASSISTANT);
        inventory.recordMovement(id, movement(MovementType.OUT, 3), MovementSource.MANUAL);
        inventory.recordMovement(id, movement(MovementType.WASTE, 2), MovementSource.MANUAL);
        MovementView count = inventory.recordMovement(id, movement(MovementType.ADJUSTMENT, 4),
                MovementSource.MANUAL);

        assertThat(count.delta()).isEqualTo(-6);
        assertThat(inventory.get(id).stock()).isEqualTo(4);
        assertThat(inventory.get(id).lowStock()).isTrue();
        assertThat(inventory.movements(id)).extracting(MovementView::delta)
                .containsExactly(-6, -2, -3, 5, 10);
    }

    @Test
    void stockNeverGoesNegative() {
        long id = create("Lirio", 2).id();

        assertThatThrownBy(() -> inventory.recordMovement(id, movement(MovementType.OUT, 3),
                MovementSource.MANUAL)).isInstanceOf(InventoryRuleException.class);
        assertThatThrownBy(() -> inventory.recordMovement(id, movement(MovementType.IN, 0),
                MovementSource.MANUAL)).isInstanceOf(InventoryRuleException.class);
        assertThat(inventory.get(id).stock()).isEqualTo(2);
        assertThat(inventory.movements(id)).hasSize(1);
    }

    @Test
    void simultaneousMovementsAreNotLost() throws Exception {
        long id = create("Clavel", 0).id();
        List<Callable<MovementView>> tasks = IntStream.range(0, 20)
                .<Callable<MovementView>>mapToObj(i -> () -> inventory.recordMovement(id,
                        movement(MovementType.IN, 1), MovementSource.MANUAL))
                .toList();

        try (var pool = Executors.newFixedThreadPool(8)) {
            for (var result : pool.invokeAll(tasks)) {
                result.get();
            }
        }

        assertThat(inventory.get(id).stock()).isEqualTo(20);
    }

    @Test
    void deactivatedProductsAreHiddenButKeepHistory() {
        String name = "Tulipan " + UUID.randomUUID();
        long id = create(name, 3).id();

        inventory.deactivate(id);

        assertThat(inventory.search(name, false)).isEmpty();
        assertThat(inventory.search(name, true)).hasSize(1);
        assertThat(inventory.movements(id)).hasSize(1);
        assertThatThrownBy(() -> inventory.recordMovement(id, movement(MovementType.IN, 1),
                MovementSource.MANUAL)).isInstanceOf(InventoryRuleException.class);
    }

    @Test
    void searchTreatsWildcardsAsText() {
        create("Orquidea", 1);

        assertThat(inventory.search("%", false)).isEmpty();
        assertThat(inventory.search("ORQUI", false)).isNotEmpty();
    }

    private static LocalDate inDays(int days) {
        return LocalDate.now(ZoneId.of("America/Bogota")).plusDays(days);
    }

    private void receive(long id, int quantity, LocalDate expiresOn) {
        inventory.recordMovement(id, new NewMovement(MovementType.IN, quantity, "compra", expiresOn, null),
                MovementSource.MANUAL);
    }

    @Test
    void outgoingStockUsesTheLotThatExpiresFirst() {
        long id = create("Astromelia", 0).id();
        receive(id, 10, null);
        receive(id, 10, inDays(8));
        receive(id, 10, inDays(2));

        inventory.recordMovement(id, movement(MovementType.OUT, 15), MovementSource.MANUAL);

        assertThat(inventory.lots(id)).extracting(LotView::expiresOn, LotView::quantity)
                .containsExactly(tuple(inDays(8), 5), tuple(null, 10));
        assertThat(inventory.get(id).nextExpiry()).isEqualTo(inDays(8));
    }

    @Test
    void expiredFlowersCanOnlyLeaveAsWaste() {
        long id = create("Margarita", 0).id();
        receive(id, 6, inDays(1));
        receive(id, 4, inDays(10));
        // El tiempo no se puede adelantar: se envejece el primer lote en la base.
        jdbc.update("update stock_lot set expires_on = ? where product_id = ? and quantity = 6",
                inDays(-1), id);

        assertThat(inventory.get(id).expiredStock()).isEqualTo(6);
        assertThat(inventory.get(id).lowStock()).isTrue();
        assertThat(inventory.expiringLots(0)).anyMatch(l -> l.productId() == id && l.expired());
        assertThatThrownBy(() -> inventory.recordMovement(id, movement(MovementType.OUT, 5),
                MovementSource.MANUAL)).isInstanceOf(InventoryRuleException.class);
        assertThat(inventory.get(id).stock()).isEqualTo(10);
        assertThat(inventory.lots(id)).extracting(LotView::quantity).containsExactly(6, 4);

        inventory.recordMovement(id, movement(MovementType.WASTE, 6), MovementSource.MANUAL);

        assertThat(inventory.get(id).expiredStock()).isZero();
        assertThat(inventory.get(id).stock()).isEqualTo(4);
    }

    @Test
    void aSpecificLotCanBeChosenAndCountsKeepLotsInSync() {
        long id = create("Gerbera", 0).id();
        receive(id, 3, inDays(2));
        receive(id, 5, inDays(9));
        long later = inventory.lots(id).get(1).id();

        inventory.recordMovement(id, new NewMovement(MovementType.WASTE, 2, "golpeadas", null, later),
                MovementSource.MANUAL);
        assertThat(inventory.lots(id)).extracting(LotView::quantity).containsExactly(3, 3);

        inventory.recordMovement(id, movement(MovementType.ADJUSTMENT, 2), MovementSource.MANUAL);
        assertThat(inventory.lots(id)).extracting(LotView::quantity).containsExactly(2);

        inventory.recordMovement(id, movement(MovementType.ADJUSTMENT, 7), MovementSource.MANUAL);
        assertThat(inventory.lots(id)).extracting(LotView::quantity).containsExactly(2, 5);

        assertThatThrownBy(() -> receive(id, 1, inDays(-1))).isInstanceOf(InventoryRuleException.class);
        assertThat(inventory.get(id).stock()).isEqualTo(7);
    }

    @Test
    void apiValidatesAndReportsErrors() throws Exception {
        String body = """
                {"name":"Hortensia","category":"Flores","unit":"STEM","sku":"hz-1",
                 "minStock":2,"unitCost":9500,"unitPrice":18500,"initialStock":7}
                """;
        mvc.perform(post("/api/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("HZ-1"))
                .andExpect(jsonPath("$.stock").value(7));
        mvc.perform(post("/api/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/expiring-lots"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound());
    }
}
