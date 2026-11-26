package com.dore.customers;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class CustomerRepository {

    private static final String SELECT = """
            select c.id, c.name, c.phone, c.email, c.notes,
              (select count(*) from customer_order o
                where o.customer_id = c.id and o.status <> 'CANCELLED') as orders_count,
              (select coalesce(sum(s.amount), 0) from sale s
                where s.customer_id = c.id) as total_spent
            from customer c
            """;

    private final JdbcClient jdbc;

    CustomerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<CustomerView> findAll() {
        return jdbc.sql(SELECT + " order by c.name, c.id").query(CustomerView.class).list();
    }

    CustomerView find(long id) {
        return jdbc.sql(SELECT + " where c.id = ?").param(id).query(CustomerView.class).optional()
                .orElse(null);
    }

    long insert(CustomerData data) {
        return jdbc.sql("insert into customer (name, phone, email, notes) values (?,?,?,?) returning id")
                .params(data.name().strip(), text(data.phone()), text(data.email()), text(data.notes()))
                .query(Long.class).single();
    }

    int update(long id, CustomerData data) {
        return jdbc.sql("update customer set name = ?, phone = ?, email = ?, notes = ? where id = ?")
                .params(data.name().strip(), text(data.phone()), text(data.email()), text(data.notes()), id)
                .update();
    }

    private static String text(String value) {
        return value == null ? "" : value.strip();
    }
}
