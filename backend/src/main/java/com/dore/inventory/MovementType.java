package com.dore.inventory;

public enum MovementType {
    /** Entrada: suma al stock. */
    IN,
    /** Salida: resta del stock. */
    OUT,
    /** Merma (flor dañada o vencida): resta del stock. */
    WASTE,
    /** Conteo físico: la cantidad indicada pasa a ser el stock. */
    ADJUSTMENT
}
