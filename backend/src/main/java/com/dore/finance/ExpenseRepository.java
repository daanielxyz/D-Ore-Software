package com.dore.finance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class ExpenseRepository {

    private final JdbcClient jdbc;

    ExpenseRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<ExpenseView> between(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select id, description, amount, category, expense_date from expense
                where expense_date between ? and ? order by expense_date desc, id desc
                """).params(from, to).query(ExpenseView.class).list();
    }

    long incomeBetween(LocalDate from, LocalDate to) {
        return jdbc.sql("select coalesce(sum(amount), 0) from sale where sale_date between ? and ?")
                .params(from, to).query(Long.class).single();
    }

    void insert(NewExpense expense, LocalDate date) {
        jdbc.sql("insert into expense (description, amount, category, expense_date) values (?,?,?,?)")
                .params(expense.description().strip(), expense.amount(), expense.category().name(), date)
                .update();
    }

    int delete(long id) {
        return jdbc.sql("delete from expense where id = ?").param(id).update();
    }
}
