package com.customer.account.tracker.service;

import com.customer.account.tracker.dto.CustomerResponse;
import com.customer.account.tracker.dto.CustomerUpdateRequest;
import com.customer.account.tracker.entity.Customer;
import com.customer.account.tracker.exception.CustomerNotFoundException;
import com.customer.account.tracker.mapper.CustomerMapper;
import com.customer.account.tracker.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponse getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customerMapper.mapAccountsToCustomerResponse(customers);
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer id, CustomerUpdateRequest updateRequest) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        Customer updatedCustomer = customerMapper.mapCustomerUpdateRequestToCustomer(existingCustomer, updateRequest);
        Customer newCustomer = customerRepository.save(updatedCustomer);
        return customerMapper.mapCustomerToCustomerResponse(newCustomer);
    }

    public CustomerResponse getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return customerMapper.mapCustomerToCustomerResponse(customer);
    }
}
