package com.dore.assistant;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dore.inventory.InventoryService;
import com.dore.inventory.LotView;
import com.dore.inventory.MovementType;
import com.dore.inventory.NewMovement;
import com.dore.inventory.NewProduct;
import com.dore.inventory.ProductView;
import com.dore.inventory.Unit;
import com.dore.shared.BusinessCalendar;
import com.dore.shared.RuleException;

/**
 * Convierte lo que propone el modelo en acciones validadas. La IA propone y el
 * backend valida: si algo no cuadra se lanza RuleException y no hay propuesta.
 */
class Planner {

    private static final Map<Unit, String> UNITS = Map.of(Unit.STEM, "tallos", Unit.BUNCH, "ramos",
            Unit.ROLL, "rollos", Unit.PACKAGE, "paquetes", Unit.SHEET, "pliegos",
            Unit.BLOCK, "bloques", Unit.UNIT, "unidades");

    private final InventoryService inventory;
    final List<Action> actions = new ArrayList<>();
    final List<Action.Preview> previews = new ArrayList<>();
    /** Stock previsto cuando una orden toca varias veces el mismo producto. */
    private final Map<Long, Integer> projected = new HashMap<>();
    private final Map<Long, Integer> projectedExpired = new HashMap<>();

    Planner(InventoryService inventory) {
        this.inventory = inventory;
    }

    void add(AssistantModel.Call call) {
        Map<String, Object> args = call.args();
        switch (call.name()) {
            case "register_movement" -> movement(args);
            case "create_product" -> create(args);
            case "update_product" -> update(args);
            case "remove_product" -> remove(args);
            default -> throw new RuleException("No entendí qué cambio hacer. Dilo de otra forma.");
        }
    }

    private void movement(Map<String, Object> args) {
        ProductView product = product(args);
        MovementType type = type(args);
        Integer quantity = integer(args, "quantity");
        if (quantity == null) {
            throw new RuleException("¿Cuántos " + UNITS.get(product.unit()) + " de " + product.name() + "?");
        }
        if (quantity < 0 || (quantity == 0 && type != MovementType.ADJUSTMENT)) {
            throw new RuleException("La cantidad debe ser mayor que cero.");
        }
        LocalDate expiresOn = date(args, "expires_on");
        if (expiresOn != null && (type == MovementType.OUT || type == MovementType.WASTE)) {
            expiresOn = null;
        }
        if (expiresOn != null && expiresOn.isBefore(BusinessCalendar.today())) {
            throw new RuleException("No se puede ingresar flor ya vencida.");
        }

        int stock = projected.getOrDefault(product.id(), product.stock());
        int expired = projectedExpired.getOrDefault(product.id(), product.expiredStock());
        long result = switch (type) {
            case IN -> (long) stock + quantity;
            case OUT, WASTE -> (long) stock - quantity;
            case ADJUSTMENT -> quantity;
        };
        int available = type == MovementType.OUT ? stock - expired : stock;
        if ((type == MovementType.OUT && quantity > available) || result < 0) {
            String detail = type == MovementType.OUT && expired > 0
                    ? " sin vencer (hay " + expired + " vencidos que van como merma)" : "";
            throw new RuleException("De " + product.name() + " solo hay " + available + " "
                    + UNITS.get(product.unit()) + detail + ".");
        }
        if (result > Integer.MAX_VALUE) {
            throw new RuleException("La cantidad es demasiado grande.");
        }
        projected.put(product.id(), (int) result);
        if (type == MovementType.WASTE) {
            projectedExpired.put(product.id(), Math.max(0, expired - quantity));
        }

        String verb = switch (type) {
            case IN -> "Entrada de";
            case OUT -> "Salida de";
            case WASTE -> "Merma de";
            case ADJUSTMENT -> "Conteo:";
        };
        String summary = verb + " " + quantity + " " + UNITS.get(product.unit()) + " de " + product.name()
                + " (" + stock + " → " + result + ")"
                + (expiresOn == null ? "" : ", vencen el " + expiresOn);
        actions.add(new Action.Move(product.id(),
                new NewMovement(type, quantity, text(args, "reason", 200), expiresOn, null)));
        previews.add(new Action.Preview("MOVEMENT", product.sku(), type, quantity, summary));
    }

    private void create(Map<String, Object> args) {
        String name = text(args, "name", 120);
        String category = text(args, "category", 60);
        Unit unit = unit(args);
        Integer price = integer(args, "unit_price");
        if (name == null || category == null || unit == null || price == null) {
            throw new RuleException("Para crear el producto necesito nombre, categoría, unidad y precio de venta.");
        }
        String sku = text(args, "sku", 40);
        if (sku != null && inventory.search(sku, true).stream().anyMatch(p -> p.sku().equalsIgnoreCase(sku))) {
            throw new RuleException("Ya existe un producto con el código " + sku + ".");
        }
        if (inventory.search(name, false).stream().anyMatch(p -> p.name().equalsIgnoreCase(name))) {
            throw new RuleException("Ya existe un producto llamado " + name + ".");
        }
        int stock = positiveOrZero(integer(args, "initial_stock"));
        int min = positiveOrZero(integer(args, "min_stock"));
        int cost = positiveOrZero(integer(args, "unit_cost"));
        actions.add(new Action.Create(new NewProduct(name, category, unit, sku, min, cost,
                positiveOrZero(price), text(args, "supplier", 120), stock, null)));
        previews.add(new Action.Preview("CREATE", sku, null, stock, "Crear " + name + " (" + category + "): "
                + stock + " " + UNITS.get(unit) + ", mínimo " + min + ", costo $" + cost + ", precio $" + price));
    }

    private void update(Map<String, Object> args) {
        ProductView product = product(args);
        Integer min = integer(args, "min_stock");
        Integer cost = integer(args, "unit_cost");
        Integer price = integer(args, "unit_price");
        var action = new Action.Update(product.id(), text(args, "name", 120), text(args, "category", 60),
                unit(args), min == null ? null : positiveOrZero(min),
                cost == null ? null : (long) positiveOrZero(cost),
                price == null ? null : (long) positiveOrZero(price), text(args, "supplier", 120));
        List<String> changes = new ArrayList<>();
        describe(changes, "nombre", product.name(), action.name());
        describe(changes, "categoría", product.category(), action.category());
        describe(changes, "unidad", UNITS.get(product.unit()), action.unit() == null ? null : UNITS.get(action.unit()));
        describe(changes, "stock mínimo", product.minStock(), action.minStock());
        describe(changes, "costo", product.unitCost(), action.unitCost());
        describe(changes, "precio", product.unitPrice(), action.unitPrice());
        describe(changes, "proveedor", product.supplier(), action.supplier());
        if (changes.isEmpty()) {
            throw new RuleException("¿Qué quieres cambiar de " + product.name() + "?");
        }
        actions.add(action);
        previews.add(new Action.Preview("UPDATE", product.sku(), null, null,
                "Cambiar " + product.name() + ": " + String.join("; ", changes)));
    }

    private void remove(Map<String, Object> args) {
        ProductView product = product(args);
        actions.add(new Action.Deactivate(product.id()));
        previews.add(new Action.Preview("REMOVE", product.sku(), null, null,
                "Eliminar " + product.name() + " del catálogo (quedan " + product.stock() + " "
                        + UNITS.get(product.unit()) + "; el historial se conserva)"));
    }

    private static void describe(List<String> changes, String label, Object before, Object after) {
        if (after != null && !after.equals(before) && !String.valueOf(after).equals(String.valueOf(before))) {
            changes.add(label + " " + before + " → " + after);
        }
    }

    /** El modelo identifica por código; se exige que exista y esté activo. */
    private ProductView product(Map<String, Object> args) {
        String sku = text(args, "sku", 40);
        return inventory.search(sku == null ? "" : sku, false).stream()
                .filter(p -> p.sku().equalsIgnoreCase(sku))
                .findFirst()
                .orElseThrow(() -> new RuleException("No encontré ese producto en el inventario."));
    }

    private static MovementType type(Map<String, Object> args) {
        try {
            return MovementType.valueOf(String.valueOf(args.get("type")));
        } catch (IllegalArgumentException error) {
            throw new RuleException("¿Es una entrada, una salida, una merma o un conteo?");
        }
    }

    private static Unit unit(Map<String, Object> args) {
        String value = text(args, "unit", 20);
        try {
            return value == null ? null : Unit.valueOf(value);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    private static String text(Map<String, Object> args, String key, int max) {
        if (!(args.get(key) instanceof String value) || value.isBlank()) {
            return null;
        }
        String clean = value.strip();
        return clean.length() > max ? clean.substring(0, max) : clean;
    }

    /** Rechaza decimales: el stock se maneja en unidades enteras. */
    private static Integer integer(Map<String, Object> args, String key) {
        if (!(args.get(key) instanceof Number number)) {
            return null;
        }
        double value = number.doubleValue();
        if (value != Math.rint(value) || Math.abs(value) > 1_000_000_000) {
            throw new RuleException("Necesito un número entero, no " + number + ".");
        }
        return (int) value;
    }

    private static int positiveOrZero(Integer value) {
        if (value != null && value < 0) {
            throw new RuleException("Los valores no pueden ser negativos.");
        }
        return value == null ? 0 : value;
    }

    private static LocalDate date(Map<String, Object> args, String key) {
        String value = text(args, key, 20);
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (DateTimeParseException error) {
            throw new RuleException("No entendí la fecha de vencimiento.");
        }
    }
}
