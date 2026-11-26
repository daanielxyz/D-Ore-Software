package com.dore.assistant;

import java.util.List;
import java.util.Map;

/** El proveedor de IA queda detrás de esta interfaz para poder cambiarlo (D-008). */
public interface AssistantModel {

    /** Un turno anterior de la conversación; fromUser distingue quién habló. */
    record Turn(boolean fromUser, String text) {
    }

    /** data va en base64. */
    record Audio(String mimeType, String data) {
    }

    record Function(String name, String description, Map<String, Object> parameters) {
    }

    record Call(String name, Map<String, Object> args) {
    }

    /** Devuelve las funciones que el modelo decidió llamar. Nunca ejecuta nada. */
    List<Call> interpret(String instructions, List<Function> functions, List<Turn> history,
            String text, Audio audio);
}
