package com.customer.account.tracker.service;

import com.customer.account.tracker.annotation.JsonFileSource;
import com.customer.account.tracker.dto.CustomerDetails;
import com.customer.account.tracker.dto.CustomerResponse;
import com.customer.account.tracker.dto.CustomerUpdateRequest;
import com.customer.account.tracker.entity.Customer;
import com.customer.account.tracker.exception.CustomerNotFoundException;
import com.customer.account.tracker.mapper.CustomerMapper;
import com.customer.account.tracker.mapper.CustomerMapperImpl;
import com.customer.account.tracker.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    CustomerService customerService;

    @Mock
    CustomerRepository customerRepository;

    CustomerMapper customerMapper = new CustomerMapperImpl();

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    @ParameterizedTest
    @DisplayName("getAllAccounts returns mapped CustomerResponse from accounts")
    @JsonFileSource(jsonPath = {"json/customer/valid-customer-input.json", "json/customer/valid-customer-input2.json"}, returnType = Customer.class)
    void getAllCustomer_returnsMappedCustomersResponse(Customer primaryCustomer, Customer secondaryCustomer) {

        when(customerRepository.findAll()).thenReturn(List.of(primaryCustomer, secondaryCustomer));

        CustomerResponse customerResponse = customerService.getAllCustomers();

        assertThat(customerResponse).isNotNull();
        assertThat(customerResponse.getCustomers())
                .isNotNull()
                .hasSize(2)
                .extracting(CustomerDetails::getFirstName, CustomerDetails::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(primaryCustomer.getFirstName(), primaryCustomer.getEmail()),
                        tuple(secondaryCustomer.getFirstName(), secondaryCustomer.getEmail()));
    }

    @Test
    @DisplayName("updateCustomer throws NotFound when id missing")
    void updateCustomer_throwsNotFound_whenMissing() {
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(1, new CustomerUpdateRequest()));
    }

    @ParameterizedTest
    @DisplayName("updateCustomer returns mapped response when update succeeds")
    @JsonFileSource(jsonPath = "json/customer/valid-customer-input.json", returnType = Customer.class)
    void updateCustomer_returnsMappedResponse_onSuccess(Customer existingCustomer) {
        when(customerRepository.findById(existingCustomer.getId())).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("Test");
        updateRequest.setLastName("NewLast");
        updateRequest.setEmail("test@example.com");
        updateRequest.setPhone("111");
        updateRequest.setAddress("New Addr");

        CustomerResponse customerResponse = customerService.updateCustomer(existingCustomer.getId(), updateRequest);

        assertThat(customerResponse).isNotNull();
        assertThat(customerResponse.getCustomers())
                .isNotNull()
                .hasSize(1)
                .extracting(CustomerDetails::getFirstName, CustomerDetails::getEmail)
                .containsExactlyInAnyOrder(
                        tuple("Test", "test@example.com"));
    }

    @Test
    @DisplayName("getCustomerById throws NotFound when id missing")
    void getCustomerById_throwsNotFound_whenMissing() {
        when(customerRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(2));
    }

    @ParameterizedTest
    @DisplayName("getCustomerById returns mapped response when found")
    @JsonFileSource(jsonPath = "json/customer/valid-customer-input.json", returnType = Customer.class)
    void getCustomerById_returnsMappedResponse_whenFound(Customer existingCustomer) {
        when(customerRepository.findById(existingCustomer.getId())).thenReturn(Optional.of(existingCustomer));

        CustomerResponse customerResponse = customerService.getCustomerById(existingCustomer.getId());

        assertThat(customerResponse).isNotNull();
        assertThat(customerResponse.getCustomers())
                .isNotNull()
                .hasSize(1)
                .extracting(CustomerDetails::getFirstName, CustomerDetails::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(existingCustomer.getFirstName(), existingCustomer.getEmail()));
    }
}
