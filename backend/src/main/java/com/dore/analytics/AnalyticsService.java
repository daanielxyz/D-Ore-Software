package com.dore.analytics;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dore.shared.BusinessCalendar;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository repository;

    AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    /** Los últimos días, contando hoy. Los días sin ventas salen en cero. */
    public AnalyticsView lastDays(int days) {
        LocalDate to = BusinessCalendar.today();
        LocalDate from = to.minusDays(Math.clamp(days, 1, 90) - 1L);
        Map<LocalDate, AnalyticsView.Day> sold = repository.salesByDay(from, to).stream()
                .collect(Collectors.toMap(AnalyticsView.Day::date, Function.identity()));
        var series = from.datesUntil(to.plusDays(1))
                .map(date -> sold.getOrDefault(date, new AnalyticsView.Day(date, 0, 0)))
                .toList();
        return new AnalyticsView(from, to, series, repository.byChannel(from, to),
                repository.topProducts(from, to), repository.waste(from, to),
                repository.expensesByCategory(from, to));
    }
}
