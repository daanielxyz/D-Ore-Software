package com.dore.assistant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Declaración de las funciones que el modelo puede llamar. */
final class Functions {

    private static final Map<String, Object> TEXT = Map.of("type", "STRING");
    private static final Map<String, Object> INTEGER = Map.of("type", "INTEGER");
    private static final Map<String, Object> NUMBER = Map.of("type", "NUMBER");
    private static final Map<String, Object> UNIT = options("STEM", "BUNCH", "ROLL", "PACKAGE", "SHEET", "BLOCK", "UNIT");

    static final List<AssistantModel.Function> ALL = List.of(
            function("register_movement", "Entrada, salida, merma o conteo de un producto existente.",
                    List.of("sku", "type", "quantity"),
                    "sku", TEXT,
                    "type", options("IN", "OUT", "WASTE", "ADJUSTMENT"),
                    "quantity", NUMBER,
                    "reason", TEXT,
                    "expires_on", describe("Vencimiento AAAA-MM-DD, solo si se menciona en una entrada."),
                    "heard", describe("Transcripción de lo que dijo la persona, solo si envió audio.")),
            function("create_product", "Crea un producto que no existe. Todos los datos obligatorios deben haber sido dichos.",
                    List.of("name", "category", "unit", "unit_price"),
                    "name", TEXT, "category", TEXT, "unit", UNIT, "sku", TEXT,
                    "initial_stock", INTEGER, "min_stock", INTEGER, "unit_cost", INTEGER,
                    "unit_price", INTEGER, "supplier", TEXT, "heard", TEXT),
            function("update_product", "Cambia datos de un producto existente. Incluye solo lo que cambia. No cambia el stock.",
                    List.of("sku"),
                    "sku", TEXT, "name", TEXT, "category", TEXT, "unit", UNIT,
                    "min_stock", INTEGER, "unit_cost", INTEGER, "unit_price", INTEGER,
                    "supplier", TEXT, "heard", TEXT),
            function("remove_product", "Quita un producto del catálogo.",
                    List.of("sku"), "sku", TEXT, "heard", TEXT),
            function("reply", "Para preguntar algo, avisar que no se puede o responder una consulta. No cambia nada.",
                    List.of("message"), "message", TEXT, "heard", TEXT));

    private Functions() {
    }

    private static Map<String, Object> options(String... values) {
        return Map.of("type", "STRING", "enum", List.of(values));
    }

    private static Map<String, Object> describe(String text) {
        return Map.of("type", "STRING", "description", text);
    }

    private static AssistantModel.Function function(String name, String description,
            List<String> required, Object... properties) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < properties.length; i += 2) {
            map.put((String) properties[i], properties[i + 1]);
        }
        return new AssistantModel.Function(name, description,
                Map.of("type", "OBJECT", "properties", map, "required", required));
    }
}
