package com.dore.customers;

import java.util.List;

import com.dore.shared.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customers;

    CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public List<CustomerView> list() {
        return customers.findAll();
    }

    public CustomerView get(long id) {
        CustomerView customer = customers.find(id);
        if (customer == null) {
            throw new NotFoundException("No existe el cliente " + id + ".");
        }
        return customer;
    }

    public CustomerView create(CustomerData data) {
        return get(customers.insert(data));
    }

    public CustomerView update(long id, CustomerData data) {
        customers.update(id, data);
        return get(id);
    }
}
