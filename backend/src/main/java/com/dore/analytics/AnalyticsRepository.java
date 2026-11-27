package com.dore.analytics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class AnalyticsRepository {

    private final JdbcClient jdbc;

    AnalyticsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<AnalyticsView.Day> salesByDay(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select sale_date as date, sum(amount) as total, count(*) as count from sale
                where sale_date between ? and ? group by sale_date
                """).params(from, to).query(AnalyticsView.Day.class).list();
    }

    List<AnalyticsView.Channel> byChannel(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select channel, sum(amount) as total, count(*) as count from sale
                where sale_date between ? and ? group by channel order by total desc, channel
                """).params(from, to).query(AnalyticsView.Channel.class).list();
    }

    /** Unidades vendidas: ventas de mostrador y pedidos entregados. */
    List<AnalyticsView.Product> topProducts(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select p.name, p.unit, sum(l.quantity) as quantity
                from (
                    select i.product_id, i.quantity from sale_item i
                    join sale s on s.id = i.sale_id where s.sale_date between ? and ?
                    union all
                    select i.product_id, i.quantity from order_item i
                    join sale s on s.order_id = i.order_id where s.sale_date between ? and ?
                ) l join product p on p.id = l.product_id
                group by p.id, p.name, p.unit order by quantity desc, p.name limit 8
                """).params(from, to, from, to).query(AnalyticsView.Product.class).list();
    }

    List<AnalyticsView.Waste> waste(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select p.name, p.unit, sum(-m.delta) as quantity, sum(-m.delta * p.unit_cost) as cost
                from stock_movement m join product p on p.id = m.product_id
                where m.type = 'WASTE'
                  and (m.created_at at time zone 'America/Bogota')::date between ? and ?
                group by p.id, p.name, p.unit order by cost desc, p.name limit 8
                """).params(from, to).query(AnalyticsView.Waste.class).list();
    }

    List<AnalyticsView.Category> expensesByCategory(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select category, sum(amount) as total from expense
                where expense_date between ? and ? group by category order by total desc, category
                """).params(from, to).query(AnalyticsView.Category.class).list();
    }
}
