package com.customer.account.tracker.repository;

import com.customer.account.tracker.annotation.JsonFileSource;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.entity.AccountType;
import com.customer.account.tracker.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CustomerRepository customerRepository;

    @ParameterizedTest
    @DisplayName("Save account and findById returns saved account using customer fixture")
    @JsonFileSource(jsonPath = "json/customer/valid-customer-input.json", returnType = Customer.class)
    void saveAndFindById_returnsSavedAccount(Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);

        Account accountToSave = buildAccount(savedCustomer, AccountType.SAVING_INDIVIDUAL, "500.00", "INR", "HDFC");

        Account savedAccount = accountRepository.save(accountToSave);

        Optional<Account> found = accountRepository.findById(savedAccount.getAccountNumber());
        assertThat(found).isPresent();
        assertThat(found.get().getCustomer().getId()).isEqualTo(savedCustomer.getId());
        assertThat(found.get().getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @ParameterizedTest
    @DisplayName("existsByCustomerId returns true when account exists for customer using customer fixture")
    @JsonFileSource(jsonPath = "json/customer/valid-customer-input.json", returnType = Customer.class)
    void existsByCustomerId_returnsTrue_whenAccountExists(Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);

        Account accountToSave = buildAccount(savedCustomer, AccountType.CURRENT, "250.00", "USD", "CITI");
        accountRepository.save(accountToSave);

        boolean exists = accountRepository.existsByCustomerId(savedCustomer.getId());
        assertThat(exists).isTrue();
    }

    private Account buildAccount(Customer customer, AccountType type, String balance, String currency, String bankName) {
        Account account = new Account();
        account.setAccountType(type);
        account.setBalance(new BigDecimal(balance));
        account.setCustomer(customer);
        account.setCurrency(currency);
        account.setAccountOpeningDate(LocalDateTime.now());
        account.setAccountActive(Boolean.TRUE);
        account.setBankName(bankName);
        return account;
    }
}
