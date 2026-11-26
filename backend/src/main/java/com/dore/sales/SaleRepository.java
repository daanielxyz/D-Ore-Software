package com.dore.sales;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class SaleRepository {

    private final JdbcClient jdbc;

    SaleRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<SaleView> findRecent() {
        return jdbc.sql("""
                select s.id, s.description, s.amount, s.channel, s.sale_date, s.customer_id,
                       c.name as customer_name, s.order_id
                from sale s left join customer c on c.id = s.customer_id
                order by s.sale_date desc, s.id desc limit 200
                """).query(SaleView.class).list();
    }

    boolean customerExists(long id) {
        return jdbc.sql("select count(*) from customer where id = ?").param(id)
                .query(Integer.class).single() > 0;
    }

    void insert(NewSale sale, LocalDate date, Long orderId) {
        jdbc.sql("""
                insert into sale (description, amount, channel, sale_date, customer_id, order_id)
                values (?,?,?,?,?,?)
                """)
                .params(sale.description().strip(), sale.amount(), sale.channel().name(), date,
                        sale.customerId(), orderId)
                .update();
    }
}
