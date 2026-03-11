package com.customer.account.tracker.repository;

import com.customer.account.tracker.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    @DisplayName("Find by customer mobile number returns empty when not present")
    void findByMobileNumber_returnsEmpty_whenNotPresent() {
        Optional<Customer> c = customerRepository.findByMobileNumber("9999999999");
        assertThat(c).isEmpty();
    }

/*    @ParameterizedTest
    @DisplayName("Save and find by customer mobile number returns saved customer")
    @JsonFileSource(jsonPath = "json/customer/valid-customer-input2.json", returnType = Customer.class)
    void saveAndFindByMobileNumber_returnsSavedCustomer(Customer customer) {
        Customer saved = customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findByMobileNumber(customer.getMobileNumber());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getFirstName()).isEqualTo(customer.getFirstName());
    }*/
}
