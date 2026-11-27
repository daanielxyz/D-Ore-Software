package com.dore.content;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** El alcance y las interacciones se anotan a mano. */
public record PostData(
        @NotBlank @Size(max = 120) String title,
        @NotNull Platform platform,
        @NotNull PostStatus status,
        LocalDate scheduledDate,
        @Min(0) int reach,
        @Min(0) int interactions,
        @Size(max = 300) String notes) {
}
