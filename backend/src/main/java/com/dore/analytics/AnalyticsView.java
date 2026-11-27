package com.dore.analytics;

import java.time.LocalDate;
import java.util.List;

import com.dore.shared.SalesChannel;

public record AnalyticsView(
        LocalDate from,
        LocalDate to,
        List<Day> salesByDay,
        List<Channel> byChannel,
        List<Product> topProducts,
        List<Waste> waste,
        List<Category> expensesByCategory) {

    public record Day(LocalDate date, long total, int count) {
    }

    public record Channel(SalesChannel channel, long total, int count) {
    }

    public record Product(String name, String unit, long quantity) {
    }

    /** Costo calculado con el costo actual del producto. */
    public record Waste(String name, String unit, long quantity, long cost) {
    }

    public record Category(String category, long total) {
    }
}
