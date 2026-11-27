package com.dore.finance;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Sin fecha se toma el día de hoy en Cali. */
public record NewExpense(
        @NotBlank @Size(max = 200) String description,
        @Min(1) long amount,
        @NotNull ExpenseCategory category,
        LocalDate expenseDate) {
}
