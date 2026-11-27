package com.dore.finance;

import java.time.LocalDate;

public record ExpenseView(long id, String description, long amount, ExpenseCategory category,
        LocalDate expenseDate) {
}
