package com.dore.sales;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
class SaleController {

    private final SaleService sales;

    SaleController(SaleService sales) {
        this.sales = sales;
    }

    @GetMapping
    List<SaleView> recent() {
        return sales.recent();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void register(@Valid @RequestBody NewSale sale) {
        sales.register(sale);
    }
}
