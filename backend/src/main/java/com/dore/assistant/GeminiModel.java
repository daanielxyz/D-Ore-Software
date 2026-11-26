package com.dore.assistant;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Llama a generateContent de Gemini. Una sola petición por orden: el modelo debe
 * responder siempre con llamadas a funciones (modo ANY).
 */
@Component
class GeminiModel implements AssistantModel {

    private static final Logger log = LoggerFactory.getLogger(GeminiModel.class);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String key;
    /** En orden de preferencia: los siguientes son respaldo. */
    private final List<String> models;
    private final RestClient client;

    GeminiModel(@Value("${app.gemini.key:}") String key, @Value("${app.gemini.models}") List<String> models) {
        this.key = key;
        this.models = models;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.client = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<Call> interpret(String instructions, List<Function> functions, List<Turn> history,
            String text, Audio audio) {
        if (key.isBlank()) {
            throw new AssistantUnavailableException("El asistente no está configurado: falta GEMINI_API_KEY.");
        }
        List<Object> contents = new ArrayList<>();
        for (Turn turn : history) {
            contents.add(content(turn.fromUser() ? "user" : "model", List.of(Map.of("text", turn.text()))));
        }
        List<Object> parts = new ArrayList<>();
        if (audio != null) {
            parts.add(Map.of("inlineData", Map.of("mimeType", audio.mimeType(), "data", audio.data())));
        }
        parts.add(Map.of("text", text == null || text.isBlank() ? "Atiende la orden del audio." : text));
        contents.add(content("user", parts));

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", instructions))),
                "contents", contents,
                "tools", List.of(Map.of("functionDeclarations", functions.stream()
                        .map(f -> Map.of("name", f.name(), "description", f.description(),
                                "parameters", f.parameters()))
                        .toList())),
                "toolConfig", Map.of("functionCallingConfig", Map.of("mode", "ANY")));
        return calls(send(body));
    }

    /**
     * Repetir es seguro porque la petición no cambia nada. Se pasa al siguiente
     * modelo si el actual está saturado, sin cupo o no responde.
     */
    private Map<String, Object> send(Map<String, Object> body) {
        String problem = "El servicio de IA no pudo atender la orden.";
        for (String model : models) {
            try {
                // La clave va en una cabecera para que no quede en direcciones ni registros.
                byte[] raw = client.post()
                        .uri("/models/{model}:generateContent", model)
                        .header("x-goog-api-key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(byte[].class);
                return parse(raw);
            } catch (RestClientResponseException error) {
                int status = error.getStatusCode().value();
                log.warn("{} respondió {}: {}", model, status, error.getResponseBodyAsString());
                if (status == 429) {
                    problem = "Se alcanzó el límite gratuito de Gemini. Espera un minuto e intenta de nuevo.";
                } else if (status < 500 && status != 404) {
                    break;
                } else {
                    problem = "El servicio de IA está saturado. Intenta de nuevo en un momento.";
                }
            } catch (RestClientException error) {
                log.warn("No se pudo llamar a {}", model, error);
                problem = "No hay conexión con el servicio de IA.";
            }
        }
        throw new AssistantUnavailableException(problem);
    }

    /** La respuesta no siempre declara que es JSON, así que se lee como bytes. */
    private static Map<String, Object> parse(byte[] raw) {
        try {
            return JSON.readValue(raw == null ? new byte[0] : raw, new TypeReference<Map<String, Object>>() {
            });
        } catch (JacksonException error) {
            String start = raw == null ? "" : new String(raw, StandardCharsets.UTF_8);
            log.warn("Respuesta inesperada de Gemini: {}", start.substring(0, Math.min(300, start.length())));
            throw new AssistantUnavailableException("El servicio de IA respondió algo inesperado.");
        }
    }

    private static Map<String, Object> content(String role, List<?> parts) {
        return Map.of("role", role, "parts", parts);
    }

    /** Extrae las llamadas a funciones de candidates[0].content.parts. */
    @SuppressWarnings("unchecked")
    static List<Call> calls(Map<String, Object> response) {
        List<Call> calls = new ArrayList<>();
        if (response != null && response.get("candidates") instanceof List<?> candidates && !candidates.isEmpty()
                && candidates.getFirst() instanceof Map<?, ?> candidate
                && candidate.get("content") instanceof Map<?, ?> content
                && content.get("parts") instanceof List<?> parts) {
            for (Object part : parts) {
                if (part instanceof Map<?, ?> map && map.get("functionCall") instanceof Map<?, ?> call
                        && call.get("name") instanceof String name) {
                    Object args = call.get("args");
                    calls.add(new Call(name, args instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of()));
                }
            }
        }
        return calls;
    }
}
