package com.dore.assistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dore.inventory.InventoryRuleException;
import com.dore.inventory.InventoryService;
import com.dore.inventory.MovementSource;
import com.dore.inventory.MovementType;
import com.dore.inventory.NewMovement;
import com.dore.inventory.NewProduct;
import com.dore.inventory.ProductView;
import com.dore.inventory.Unit;
import com.dore.shared.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** El modelo se simula: aquí se prueban las reglas, no a Gemini. */
@SpringBootTest(properties = "app.gemini.key=")
@Testcontainers
class AssistantServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    AssistantService assistant;
    @Autowired
    InventoryService inventory;
    @MockitoBean
    AssistantModel model;

    private ProductView product(int stock) {
        String sku = "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return inventory.create(new NewProduct("Flor " + sku, "Flores", Unit.STEM, sku, 2, 100, 300, "",
                stock, null), MovementSource.MANUAL);
    }

    private static AssistantModel.Call move(ProductView p, String type, Number quantity) {
        return new AssistantModel.Call("register_movement",
                Map.of("sku", p.sku(), "type", type, "quantity", quantity));
    }

    private AssistantReply say(AssistantModel.Call... calls) {
        when(model.interpret(any(), any(), any(), any(), any())).thenReturn(List.of(calls));
        return assistant.interpret("admin", "orden", null, new ArrayList<>());
    }

    @Test
    void nothingChangesUntilTheUserConfirms() {
        ProductView rose = product(10);

        AssistantReply reply = say(move(rose, "IN", 5));

        assertThat(reply.proposal()).isNotNull();
        assertThat(inventory.get(rose.id()).stock()).isEqualTo(10);
        assertThat(inventory.movements(rose.id())).hasSize(1);

        assistant.confirm("admin", reply.proposal().id());

        assertThat(inventory.get(rose.id()).stock()).isEqualTo(15);
        assertThat(inventory.movements(rose.id()).getFirst().source()).isEqualTo(MovementSource.ASSISTANT);
        assertThatThrownBy(() -> assistant.confirm("admin", reply.proposal().id()))
                .isInstanceOf(NotFoundException.class);
        assertThat(inventory.get(rose.id()).stock()).isEqualTo(15);
    }

    @Test
    void cancelledOrForeignProposalsNeverRun() {
        ProductView rose = product(10);
        String cancelled = say(move(rose, "OUT", 3)).proposal().id();
        assistant.cancel("admin", cancelled);
        String mine = say(move(rose, "OUT", 3)).proposal().id();

        assertThatThrownBy(() -> assistant.confirm("admin", cancelled)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> assistant.confirm("otra", mine)).isInstanceOf(NotFoundException.class);
        assertThat(inventory.get(rose.id()).stock()).isEqualTo(10);
    }

    @Test
    void impossibleOrdersProduceNoProposal() {
        ProductView rose = product(7);

        assertThat(say(move(rose, "OUT", 15)).proposal()).isNull();
        assertThat(say(move(rose, "IN", -10)).proposal()).isNull();
        assertThat(say(move(rose, "OUT", 0)).proposal()).isNull();
        assertThat(say(move(rose, "OUT", 2.5)).proposal()).isNull();
        assertThat(say(move(rose, "OUT", 4), move(rose, "OUT", 4)).proposal()).isNull();
        assertThat(say(new AssistantModel.Call("register_movement",
                Map.of("sku", "NO-EXISTE", "type", "IN", "quantity", 1))).proposal()).isNull();
        assertThat(say(new AssistantModel.Call("reply", Map.of("message", "¿Cuál rosa?")),
                move(rose, "OUT", 1)).proposal()).isNull();
        assertThat(inventory.get(rose.id()).stock()).isEqualTo(7);
    }

    @Test
    void createUpdateAndRemoveWork() {
        String name = "Astromelia " + UUID.randomUUID().toString().substring(0, 6);
        AssistantReply create = say(new AssistantModel.Call("create_product", Map.of(
                "name", name, "category", "Flores", "unit", "BUNCH", "unit_price", 7500, "initial_stock", 20)));
        assertThat(inventory.search(name, false)).isEmpty();
        assistant.confirm("admin", create.proposal().id());
        ProductView created = inventory.search(name, false).getFirst();
        assertThat(created.stock()).isEqualTo(20);

        assistant.confirm("admin", say(new AssistantModel.Call("update_product",
                Map.of("sku", created.sku(), "unit_price", 8000))).proposal().id());
        assertThat(inventory.get(created.id()).unitPrice()).isEqualTo(8000);
        assertThat(inventory.get(created.id()).category()).isEqualTo("Flores");

        assistant.confirm("admin", say(new AssistantModel.Call("remove_product",
                Map.of("sku", created.sku()))).proposal().id());
        assertThat(inventory.get(created.id()).active()).isFalse();
    }

    @Test
    void aProposalRunsCompletelyOrNotAtAll() {
        ProductView first = product(10);
        ProductView second = product(5);
        String id = say(move(first, "OUT", 2), move(second, "OUT", 5)).proposal().id();
        // El stock cambia entre la propuesta y la confirmación.
        inventory.recordMovement(second.id(), new NewMovement(MovementType.OUT, 1, "otra venta"),
                MovementSource.MANUAL);

        assertThatThrownBy(() -> assistant.confirm("admin", id)).isInstanceOf(InventoryRuleException.class);

        assertThat(inventory.get(first.id()).stock()).isEqualTo(10);
        assertThat(inventory.get(second.id()).stock()).isEqualTo(4);
    }

    @Test
    void readsFunctionCallsFromTheGeminiResponseShape() {
        Map<String, Object> response = Map.of("candidates", List.of(Map.of("content", Map.of("parts", List.of(
                Map.of("text", "ignorado"),
                Map.of("functionCall", Map.of("name", "reply", "args", Map.of("message", "hola"))))))));

        assertThat(GeminiModel.calls(response))
                .containsExactly(new AssistantModel.Call("reply", Map.of("message", "hola")));
        assertThat(GeminiModel.calls(Map.of())).isEmpty();
    }
}
