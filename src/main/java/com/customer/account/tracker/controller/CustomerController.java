package com.customer.account.tracker.controller;

import com.customer.account.tracker.dto.CustomerResponse;
import com.customer.account.tracker.dto.CustomerUpdateRequest;
import com.customer.account.tracker.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Validated
@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        Objects.requireNonNull(customerService, "CustomerService should not be null");
        this.customerService = customerService;
    }

    @GetMapping("/v1/api/customers")
    public ResponseEntity<CustomerResponse> getCustomers() {
        CustomerResponse customerResponse = customerService.getAllCustomers();
        return ResponseEntity.ok(customerResponse);
    }

    @PutMapping("/v1/api/customers/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Integer id, @Valid @RequestBody CustomerUpdateRequest customerUpdateRequest) {
        CustomerResponse customerResponse = customerService.updateCustomer(id, customerUpdateRequest);
        return ResponseEntity.ok(customerResponse);
    }

    @GetMapping("/v1/api/customers/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Integer id) {
        CustomerResponse customerResponse = customerService.getCustomerById(id);
        return ResponseEntity.ok(customerResponse);
    }
}
