package com.dore.shared;

import java.time.LocalDate;
import java.time.ZoneId;

public final class BusinessCalendar {

    /** El día del negocio es el de Cali, no el del servidor. */
    public static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private BusinessCalendar() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
