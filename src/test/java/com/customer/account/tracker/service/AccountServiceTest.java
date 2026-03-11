package com.customer.account.tracker.service;

import com.customer.account.tracker.annotation.JsonFileSource;
import com.customer.account.tracker.dto.*;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.entity.Customer;
import com.customer.account.tracker.exception.AccountAlreadyDeactivatedException;
import com.customer.account.tracker.exception.AccountAlreadyExistsException;
import com.customer.account.tracker.exception.AccountNotFoundException;
import com.customer.account.tracker.exception.InsufficientBalanceException;
import com.customer.account.tracker.mapper.AccountMapper;
import com.customer.account.tracker.mapper.AccountMapperImpl;
import com.customer.account.tracker.mapper.CustomerMapper;
import com.customer.account.tracker.mapper.CustomerMapperImpl;
import com.customer.account.tracker.repository.AccountRepository;
import com.customer.account.tracker.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    AccountService accountService;

    @Mock
    AccountRepository accountRepository;

    @Mock
    CustomerRepository customerRepository;

    private final AccountMapper accountMapper = new AccountMapperImpl();
    private final CustomerMapper customerMapper = new CustomerMapperImpl();

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, customerRepository, customerMapper, accountMapper);
    }

    @Test
    @DisplayName("getAllAccounts returns mapped AccountsResponse")
    void getAllAccounts_returnsMapped() {
        Account a1 = new Account();
        Account a2 = new Account();
        when(accountRepository.findAll()).thenReturn(List.of(a1, a2));
        AccountsResponse expected = accountMapper.mapAccountsToAccountResponse(List.of(a1, a2));

        AccountsResponse actual = accountService.getAllAccounts();
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("transferFunds succeeds and returns success response")
    @JsonFileSource(jsonPath = {"json/account/from-account.json", "json/account/to-account.json"}, returnType = Account.class)
    void transferFunds_succeeds_fromAndToFixtures(Account fromAccount, Account toAccount) {
        // Arrange
        fromAccount.setBalance(new BigDecimal("200.00"));
        toAccount.setBalance(new BigDecimal("50.00"));

        when(accountRepository.findById(fromAccount.getAccountNumber())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getAccountNumber())).thenReturn(Optional.of(toAccount));

        // Act
        TransferResponse resp = accountService.transferFunds(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), new BigDecimal("100.00"));

        // Assert
        assertThat(resp.getStatus()).isEqualTo("SUCCESS");
        assertThat(fromAccount.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("transferFunds throws AccountNotFound when destination missing")
    void transferFunds_throwsAccountNotFound_whenToMissing() {
        when(accountRepository.findById(1)).thenReturn(Optional.of(new Account()));
        when(accountRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.transferFunds(1, 2, new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("transferFunds throws IllegalArgument when transfer to self")
    void transferFunds_throwsIllegalArgument_whenToSelf() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.transferFunds(1, 1, new BigDecimal("10.00")));
    }

    @ParameterizedTest
    @JsonFileSource(jsonPath = {"json/account/from-account.json", "json/account/to-account.json"}, returnType = Account.class)
    @DisplayName("transferFunds throws InsufficientBalance when amount larger than balance")
    void transferFunds_throwsInsufficientBalance_whenAmountTooLarge(Account fromAccount, Account toAccount) {
        fromAccount.setBalance(new BigDecimal("5.00"));
        when(accountRepository.findById(1)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.transferFunds(1, 2, new BigDecimal("10.00")));
    }

    @ParameterizedTest
    @DisplayName("getBalanceOf returns mapped AccountDto when account found")
    @JsonFileSource(jsonPath = "json/account/from-account.json", returnType = Account.class)
    void getBalanceOf_returnsMapped_whenFound(Account account) {
        when(accountRepository.findById(account.getAccountNumber())).thenReturn(Optional.of(account));
        AccountDto expected = accountMapper.mapAccountToAccountDto(account);

        AccountDto actual = accountService.getBalanceOf(account.getAccountNumber());
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("getBalanceOf throws AccountNotFound when missing")
    void getBalanceOf_throwsWhenMissing() {
        when(accountRepository.findById(8)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getBalanceOf(8));
    }

    @ParameterizedTest
    @DisplayName("createAccount persists account for new customer")
    @JsonFileSource(jsonPath = "json/account/create-account-valid.json", returnType = CreateAccountRequest.class)
    void createAccount_savesAndReturnsCreatedAccount_forNewCustomer(CreateAccountRequest accountRequest) {
        when(customerRepository.findByMobileNumber(accountRequest.getMobileNumber())).thenReturn(Optional.empty());

        Customer saved = customerMapper.mapAccountRequestToCustomer(accountRequest);
        saved.setId(10);
        saved.getAccounts().forEach(a -> a.setAccountNumber(99));
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        Account created = accountService.createAccount(accountRequest);
        assertThat(created).isNotNull();
        assertThat(created.getAccountNumber()).isEqualTo(99);
    }

    @ParameterizedTest
    @DisplayName("createAccount attaches account to existing customer when allowed")
    @JsonFileSource(jsonPath = "json/account/create-account-valid.json", returnType = CreateAccountRequest.class)
    void createAccount_attachesAccount_whenCustomerExists(CreateAccountRequest accountRequest) {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1);
        Account existingAcc = new Account();
        existingAcc.setBankName("OTHER_BANK");
        existingAcc.setMobileNumber(accountRequest.getMobileNumber());
        existingAcc.setAccountActive(true);
        existingCustomer.addAccount(existingAcc);

        when(customerRepository.findByMobileNumber(accountRequest.getMobileNumber())).thenReturn(Optional.of(existingCustomer));

        Customer saved = new Customer();
        saved.setId(existingCustomer.getId());
        saved.addAccount(existingAcc);

        Account newAccount = accountMapper.mapAccountRequestToAccount(accountRequest, saved);
        newAccount.setAccountNumber(55);
        saved.addAccount(newAccount);

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        Account created = accountService.createAccount(accountRequest);
        assertThat(created.getAccountNumber()).isEqualTo(55);
    }

    @ParameterizedTest
    @DisplayName("createAccount throws when duplicate active account exists")
    @JsonFileSource(jsonPath = "json/account/create-account-valid.json", returnType = CreateAccountRequest.class)
    void createAccount_throwsAccountAlreadyExists_whenDuplicateActive(CreateAccountRequest request) {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1);
        Account duplicate = new Account();
        duplicate.setAccountActive(true);
        duplicate.setBankName(request.getBankName());
        duplicate.setMobileNumber(request.getMobileNumber());
        existingCustomer.addAccount(duplicate);

        when(customerRepository.findByMobileNumber(request.getMobileNumber())).thenReturn(Optional.of(existingCustomer));

        assertThrows(AccountAlreadyExistsException.class, () -> accountService.createAccount(request));
    }

    @Test
    @DisplayName("createAccount throws when repository.save fails to return account")
    void createAccount_throwsWhenSaveReturnsNoAccount() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setMobileNumber("7778889999");
        req.setBankName("HDFC");
        req.setAccountType(null);

        when(customerRepository.findByMobileNumber(req.getMobileNumber())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(new Customer());

        assertThrows(RuntimeException.class, () -> accountService.createAccount(req));
    }

    @ParameterizedTest
    @DisplayName("deactivateAccount deactivates active account and returns response (fixture-driven)")
    @JsonFileSource(jsonPath = "json/account/deactivate-valid.json", returnType = DeactivateAccountRequest.class)
    void deactivateAccount_succeeds_forActiveAccount(DeactivateAccountRequest deactivateAccountRequest) {
        Account active = new Account();
        active.setAccountNumber(12);
        active.setAccountActive(true);

        when(accountRepository.findById(12)).thenReturn(Optional.of(active));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeactivateAccountResponse deactivateAccountResponse = accountService.deactivateAccount(12, deactivateAccountRequest);

        assertThat(deactivateAccountResponse.getStatus()).isEqualTo("DEACTIVATED");
        assertThat(deactivateAccountResponse.getAccountNumber()).isEqualTo(12);
        assertThat(deactivateAccountResponse.getMessage()).isEqualTo(deactivateAccountRequest.getMessage());
        assertThat(deactivateAccountResponse.getDeactivatedAt()).isNotNull();
    }

    @ParameterizedTest
    @DisplayName("deactivateAccount throws when account already deactivated (fixture-driven)")
    @JsonFileSource(jsonPath = "json/account/inactive-account.json", returnType = Account.class)
    void deactivateAccount_throwsWhenAlreadyDeactivated(Account inactiveAccount) {
        when(accountRepository.findById(inactiveAccount.getAccountNumber())).thenReturn(Optional.of(inactiveAccount));
        assertThrows(AccountAlreadyDeactivatedException.class,
                () -> accountService.deactivateAccount(inactiveAccount.getAccountNumber(), new DeactivateAccountRequest()));
    }

    @Test
    @DisplayName("deactivateAccount throws AccountNotFound when account missing")
    void deactivateAccount_throwsWhenMissing() {
        when(accountRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> accountService.deactivateAccount(999, new DeactivateAccountRequest()));
    }
}
