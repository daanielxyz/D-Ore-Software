package com.dore.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.dore.content.ContentService;
import com.dore.content.Platform;
import com.dore.content.PostData;
import com.dore.content.PostStatus;
import com.dore.finance.ExpenseCategory;
import com.dore.finance.FinanceMonth;
import com.dore.finance.FinanceService;
import com.dore.finance.NewExpense;
import com.dore.inventory.InventoryService;
import com.dore.inventory.MovementSource;
import com.dore.inventory.MovementType;
import com.dore.inventory.NewMovement;
import com.dore.inventory.NewProduct;
import com.dore.inventory.Unit;
import com.dore.listening.ListeningService;
import com.dore.listening.NewMention;
import com.dore.listening.Sentiment;
import com.dore.sales.NewSale;
import com.dore.sales.SaleService;
import com.dore.shared.NotFoundException;
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
class BusinessScreensTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    AnalyticsService analytics;
    @Autowired
    FinanceService finance;
    @Autowired
    ContentService content;
    @Autowired
    ListeningService listening;
    @Autowired
    InventoryService inventory;
    @Autowired
    SaleService sales;

    @Test
    void financeAndAnalyticsAreCalculatedFromWhatWasRecorded() {
        long rose = inventory.create(new NewProduct("Rosa analítica", "Flores", Unit.STEM, null, 0, 1000,
                3000, "", 20, null), MovementSource.MANUAL).id();
        sales.register(new NewSale(null, null, SalesChannel.STORE, null, null,
                List.of(new NewSale.Line(rose, 4))));
        inventory.recordMovement(rose, new NewMovement(MovementType.WASTE, 3, "Quebradas"),
                MovementSource.MANUAL);
        finance.register(new NewExpense("Flor", 5_000, ExpenseCategory.INVENTORY, null));

        FinanceMonth month = finance.month(null);
        assertThat(month.income()).isEqualTo(12_000);
        assertThat(month.spent()).isEqualTo(5_000);

        AnalyticsView view = analytics.lastDays(7);
        assertThat(view.salesByDay()).hasSize(7);
        assertThat(view.salesByDay().getLast().total()).isEqualTo(12_000);
        assertThat(view.byChannel()).singleElement().satisfies(c -> assertThat(c.count()).isEqualTo(1));
        assertThat(view.topProducts()).singleElement().satisfies(p -> assertThat(p.quantity()).isEqualTo(4));
        assertThat(view.waste()).singleElement().satisfies(w -> assertThat(w.cost()).isEqualTo(3_000));
        assertThat(view.expensesByCategory()).singleElement()
                .satisfies(c -> assertThat(c.total()).isEqualTo(5_000));

        finance.delete(month.expenses().getFirst().id());
        assertThat(finance.month(null).spent()).isZero();
        assertThatThrownBy(() -> finance.delete(999_999)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void postsAndMentionsCanBeEditedAndRemoved() {
        assertThatThrownBy(() -> content.create(new PostData("Sin fecha", Platform.INSTAGRAM,
                PostStatus.SCHEDULED, null, 0, 0, null))).isInstanceOf(RuleException.class);
        content.create(new PostData("Idea", Platform.TIKTOK, PostStatus.IDEA, null, 0, 0, null));
        long post = content.all().getFirst().id();
        content.update(post, new PostData("Idea", Platform.TIKTOK, PostStatus.PUBLISHED, null, 100, 7, ""));
        assertThat(content.all().getFirst().reach()).isEqualTo(100);
        content.delete(post);
        assertThat(content.all()).isEmpty();

        listening.register(new NewMention(SalesChannel.INSTAGRAM, "@prueba", "Hola", Sentiment.NEUTRAL, null));
        long mention = listening.recent().getFirst().id();
        listening.setAttended(mention, true);
        assertThat(listening.recent().getFirst().attended()).isTrue();
        listening.delete(mention);
        assertThat(listening.recent()).isEmpty();
    }
}
