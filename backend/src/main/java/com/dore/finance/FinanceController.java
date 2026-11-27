package com.dore.finance;

import java.time.YearMonth;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
class FinanceController {

    private final FinanceService finance;

    FinanceController(FinanceService finance) {
        this.finance = finance;
    }

    @GetMapping
    FinanceMonth month(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return finance.month(month);
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    void register(@Valid @RequestBody NewExpense expense) {
        finance.register(expense);
    }

    @DeleteMapping("/expenses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id) {
        finance.delete(id);
    }
}
