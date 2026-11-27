package com.dore.demo;

import java.time.LocalDate;
import java.util.List;

import com.dore.content.ContentService;
import com.dore.content.Platform;
import com.dore.content.PostData;
import com.dore.content.PostStatus;
import com.dore.customers.CustomerData;
import com.dore.customers.CustomerService;
import com.dore.finance.ExpenseCategory;
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
import com.dore.orders.NewOrder;
import com.dore.orders.OrderService;
import com.dore.orders.OrderStatus;
import com.dore.sales.NewSale;
import com.dore.sales.SaleService;
import com.dore.shared.BusinessCalendar;
import com.dore.shared.SalesChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos ficticios para la demo. Solo con APP_DEMO_DATA=true y el inventario vacío.
 * Las fechas son relativas a hoy. El catálogo y el stock final coinciden con
 * prototipos/asistente/ordenes-ejemplo.md.
 */
@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true")
class DemoData implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoData.class);

    private final InventoryService inventory;
    private final CustomerService customers;
    private final OrderService orders;
    private final SaleService sales;
    private final FinanceService finance;
    private final ContentService content;
    private final ListeningService listening;
    private final JdbcClient jdbc;

    DemoData(InventoryService inventory, CustomerService customers, OrderService orders,
            SaleService sales, FinanceService finance, ContentService content,
            ListeningService listening, JdbcClient jdbc) {
        this.finance = finance;
        this.content = content;
        this.listening = listening;
        this.inventory = inventory;
        this.customers = customers;
        this.orders = orders;
        this.sales = sales;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!inventory.search("", true).isEmpty()) {
            log.info("Ya hay productos: no se cargan datos de ejemplo.");
            return;
        }
        LocalDate today = BusinessCalendar.today();

        // El stock inicial incluye lo que consumen los pedidos, ventas y mermas de ejemplo.
        long rose = product("FL-001", "Rosa Freedom roja", "Flores", Unit.STEM, 120, 30, 1800, 4200,
                "Cultivos La Montaña", today.plusDays(6));
        long ivory = product("FL-002", "Rosa Vendela marfil", "Flores", Unit.STEM, 53, 24, 2000, 4500,
                "Cultivos La Montaña", today.plusDays(2));
        long tulip = product("FL-003", "Tulipán holandés", "Flores", Unit.STEM, 51, 15, 6200, 12500,
                "Importadora Flora", today.plusDays(3));
        long hydrangea = product("FL-004", "Hortensia azul", "Flores", Unit.STEM, 7, 10, 9500, 18500,
                "Jardines del Cauca", today.plusDays(5));
        long sunflower = product("FL-005", "Girasol San Vicente", "Flores", Unit.STEM, 4, 12, 2500, 5800,
                "Agroflores Dapa", today);
        // Deja un lote vencido para mostrar la alerta. El servicio no permite ingresarlo así.
        jdbc.sql("update stock_lot set expires_on = ? where product_id = ?")
                .params(today.minusDays(1), sunflower).update();
        inventory.recordMovement(sunflower,
                new NewMovement(MovementType.IN, 26, "Compra de la mañana", today.plusDays(7), null),
                MovementSource.MANUAL);
        long eucalyptus = product("FO-001", "Eucalipto Baby Blue", "Follajes", Unit.BUNCH, 22, 20, 2400, 5500,
                "Verde Vivo", today.plusDays(9));
        product("EM-001", "Papel coreano marfil", "Empaque", Unit.SHEET, 9, 12, 2100, 4800,
                "Empaques Nativa", null);
        long ribbon = product("EM-002", "Cinta de algodón crudo", "Empaque", Unit.ROLL, 27, 10, 900, 2400,
                "Empaques Nativa", null);
        product("IN-001", "Espuma floral Oasis", "Insumos", Unit.BLOCK, 15, 8, 3200, 6500,
                "Insumos del Valle", null);

        long valentina = customer("Valentina Ríos", "Prefiere tonos suaves");
        long andres = customer("Andrés Molina", "Pedidos corporativos");
        long camila = customer("Camila Torres", "Cliente frecuente");

        order(valentina, "Cumpleaños", today, 95_000, "Tarjeta con mensaje",
                new NewOrder.Line(ivory, 8), new NewOrder.Line(tulip, 3));
        order(andres, "Corporativo", today.plusDays(1), 240_000, "Entregar en recepción",
                new NewOrder.Line(hydrangea, 4), new NewOrder.Line(sunflower, 10));
        long inProgress = order(camila, "Aniversario", today, 120_000, "",
                new NewOrder.Line(rose, 12), new NewOrder.Line(ribbon, 1));
        orders.changeStatus(inProgress, OrderStatus.IN_DESIGN);

        sale("Ramo de temporada", 85_000, SalesChannel.STORE, today, null);
        sale("Arreglo de mesa", 160_000, SalesChannel.WHATSAPP, today.minusDays(1), camila);
        sale("Bouquet de grado", 110_000, SalesChannel.INSTAGRAM, today.minusDays(3), valentina);
        sale("Centros de mesa para evento", 520_000, SalesChannel.WHATSAPP, today.minusDays(8), andres);
        // Ventas con productos y mermas: alimentan la analítica.
        counterSale(SalesChannel.STORE, today, new NewSale.Line(rose, 6), new NewSale.Line(eucalyptus, 1));
        counterSale(SalesChannel.WHATSAPP, today.minusDays(2), new NewSale.Line(tulip, 9),
                new NewSale.Line(eucalyptus, 3));
        counterSale(SalesChannel.INSTAGRAM, today.minusDays(5), new NewSale.Line(rose, 12));
        waste(rose, 6, "Tallos quebrados");
        waste(ivory, 5, "Pétalos manchados");

        expense("Compra de flor fresca", 380_000, ExpenseCategory.INVENTORY, today.minusDays(1));
        expense("Empaques y cintas", 95_000, ExpenseCategory.MATERIALS, today.minusDays(2));
        expense("Domicilios", 48_000, ExpenseCategory.DELIVERY, today.minusDays(3));
        expense("Pauta en Instagram", 60_000, ExpenseCategory.MARKETING, today.minusDays(6));
        expense("Servicios del local", 140_000, ExpenseCategory.OPERATIONS, today.minusDays(10));

        post("Colección de fin de año", Platform.INSTAGRAM, PostStatus.SCHEDULED, today.plusDays(2), 0, 0);
        post("Detrás de cada ramo", Platform.TIKTOK, PostStatus.DRAFT, null, 0, 0);
        post("Ideas para regalar sin ocasión", Platform.INSTAGRAM, PostStatus.PUBLISHED, today.minusDays(2),
                8210, 673);
        post("Arreglos para oficinas", Platform.FACEBOOK, PostStatus.IDEA, null, 0, 0);

        mention(SalesChannel.INSTAGRAM, "@laura.m", "El ramo llegó precioso, superó mis expectativas 🌷",
                Sentiment.POSITIVE, today);
        mention(SalesChannel.WHATSAPP, "Cliente frecuente",
                "¿Tienen disponibilidad para un arreglo de aniversario mañana?", Sentiment.PURCHASE_INTENT, today);
        mention(SalesChannel.INSTAGRAM, "@mariaf", "Me encantan los colores de la nueva colección",
                Sentiment.POSITIVE, today.minusDays(2));
        mention(SalesChannel.WEB, "Visitante", "El domicilio tardó más de lo que me dijeron",
                Sentiment.NEGATIVE, today.minusDays(4));
        log.info("Datos de ejemplo cargados.");
    }

    private long product(String sku, String name, String category, Unit unit, int stock, int min,
            long cost, long price, String supplier, LocalDate expiresOn) {
        return inventory.create(new NewProduct(name, category, unit, sku, min, cost, price, supplier,
                stock, expiresOn), MovementSource.MANUAL).id();
    }

    private long customer(String name, String notes) {
        return customers.create(new CustomerData(name, "", "", notes)).id();
    }

    private long order(long customer, String occasion, LocalDate delivery, long total, String notes,
            NewOrder.Line... lines) {
        return orders.create(new NewOrder(customer, occasion, SalesChannel.WHATSAPP, delivery, total,
                notes, List.of(lines))).id();
    }

    private void counterSale(SalesChannel channel, LocalDate date, NewSale.Line... lines) {
        sales.register(new NewSale(null, null, channel, date, null, List.of(lines)));
    }

    private void waste(long product, int quantity, String reason) {
        inventory.recordMovement(product, new NewMovement(MovementType.WASTE, quantity, reason),
                MovementSource.MANUAL);
    }

    private void expense(String description, long amount, ExpenseCategory category, LocalDate date) {
        finance.register(new NewExpense(description, amount, category, date));
    }

    private void post(String title, Platform platform, PostStatus status, LocalDate date, int reach,
            int interactions) {
        content.create(new PostData(title, platform, status, date, reach, interactions, ""));
    }

    private void mention(SalesChannel source, String author, String message, Sentiment sentiment,
            LocalDate date) {
        listening.register(new NewMention(source, author, message, sentiment, date));
    }

    /** Ventas históricas: sin productos, para no mover el stock actual. */
    private void sale(String description, long amount, SalesChannel channel, LocalDate date, Long customer) {
        sales.register(new NewSale(description, amount, channel, date, customer, List.of()));
    }
}
