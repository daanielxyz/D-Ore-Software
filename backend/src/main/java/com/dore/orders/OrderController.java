package com.dore.orders;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    record StatusChange(@NotNull OrderStatus status) {
    }

    private final OrderService orders;

    OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    List<OrderView> list() {
        return orders.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderView create(@Valid @RequestBody NewOrder order) {
        return orders.create(order);
    }

    @PutMapping("/{id}/status")
    OrderView changeStatus(@PathVariable long id, @Valid @RequestBody StatusChange change) {
        return orders.changeStatus(id, change.status());
    }
}
