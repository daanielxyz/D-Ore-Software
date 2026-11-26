package com.dore.shared;

/** Una operación que las reglas del negocio no permiten. */
public class RuleException extends RuntimeException {
    public RuleException(String message) {
        super(message);
    }
}
