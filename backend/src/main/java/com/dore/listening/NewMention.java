package com.dore.listening;

import java.time.LocalDate;

import com.dore.shared.SalesChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Sin fecha se toma el día de hoy en Cali. */
public record NewMention(
        @NotNull SalesChannel source,
        @NotBlank @Size(max = 80) String author,
        @NotBlank @Size(max = 500) String message,
        @NotNull Sentiment sentiment,
        LocalDate mentionDate) {
}
