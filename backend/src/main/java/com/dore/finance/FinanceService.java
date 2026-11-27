package com.dore.finance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.dore.shared.BusinessCalendar;
import com.dore.shared.NotFoundException;
import com.dore.shared.RuleException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FinanceService {

    private final ExpenseRepository expenses;

    FinanceService(ExpenseRepository expenses) {
        this.expenses = expenses;
    }

    /** Sin mes se usa el actual. */
    public FinanceMonth month(YearMonth requested) {
        YearMonth month = requested == null ? YearMonth.from(BusinessCalendar.today()) : requested;
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<ExpenseView> list = expenses.between(from, to);
        long spent = list.stream().mapToLong(ExpenseView::amount).sum();
        return new FinanceMonth(month.toString(), expenses.incomeBetween(from, to), spent, list);
    }

    public void register(NewExpense expense) {
        LocalDate date = expense.expenseDate() == null ? BusinessCalendar.today() : expense.expenseDate();
        if (date.isAfter(BusinessCalendar.today())) {
            throw new RuleException("La fecha del gasto no puede ser futura.");
        }
        expenses.insert(expense, date);
    }

    public void delete(long id) {
        if (expenses.delete(id) == 0) {
            throw new NotFoundException("No existe el gasto " + id + ".");
        }
    }
}
