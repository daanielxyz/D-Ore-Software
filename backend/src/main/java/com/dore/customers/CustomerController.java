package com.dore.customers;

import java.util.List;

import jakarta.validation.Valid;

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
@RequestMapping("/api/customers")
class CustomerController {

    private final CustomerService customers;

    CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping
    List<CustomerView> list() {
        return customers.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerView create(@Valid @RequestBody CustomerData data) {
        return customers.create(data);
    }

    @PutMapping("/{id}")
    CustomerView update(@PathVariable long id, @Valid @RequestBody CustomerData data) {
        return customers.update(id, data);
    }
}
