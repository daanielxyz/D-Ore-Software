package com.dore.finance;

import java.util.List;

/** Ingresos (ventas) y gastos registrados en un mes. */
public record FinanceMonth(String month, long income, long spent, List<ExpenseView> expenses) {
}
