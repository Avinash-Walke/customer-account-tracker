package com.customer.account.tracker.service;

import com.customer.account.tracker.dto.*;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.entity.Customer;
import com.customer.account.tracker.exception.AccountAlreadyDeactivatedException;
import com.customer.account.tracker.exception.AccountAlreadyExistsException;
import com.customer.account.tracker.exception.AccountNotFoundException;
import com.customer.account.tracker.exception.InsufficientBalanceException;
import com.customer.account.tracker.mapper.AccountMapper;
import com.customer.account.tracker.mapper.CustomerMapper;
import com.customer.account.tracker.repository.AccountRepository;
import com.customer.account.tracker.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AccountMapper accountMapper;

    @Transactional
    public Account createAccount(CreateAccountRequest accountRequest) {
        Optional<Customer> existingCustomerOpt = customerRepository.findByMobileNumber(accountRequest.getMobileNumber());

        if (existingCustomerOpt.isPresent()) {
            Customer existingCustomer = existingCustomerOpt.get();

            boolean hasActiveAccountWithSameBankAndMobile = existingCustomer.getAccounts().stream()
                    .anyMatch(account -> isAccountActiveByMobileNumber(account, accountRequest.getBankName(), accountRequest.getMobileNumber()));

            if (hasActiveAccountWithSameBankAndMobile) {
                throw new AccountAlreadyExistsException("Customer already has an active account in " + accountRequest.getBankName() + " with Mobile Number: " + accountRequest.getMobileNumber());
            }

            Account newAccount = accountMapper.mapAccountRequestToAccount(accountRequest, existingCustomer);
            existingCustomer.addAccount(newAccount);

            Customer customerSaved = customerRepository.save(existingCustomer);

            return customerSaved.getAccounts().stream()
                    .filter(account -> account.getBankName().equals(accountRequest.getBankName()) && account.getMobileNumber().equals(accountRequest.getMobileNumber()))
                    .findFirst()
                    .orElse(newAccount);
        } else {
            Customer newCustomer = customerMapper.mapAccountRequestToCustomer(accountRequest);
            Customer customerSaved = customerRepository.save(newCustomer);

            return customerSaved.getAccounts().stream()
                    .filter(account -> account.getBankName().equals(accountRequest.getBankName()) && account.getMobileNumber().equals(accountRequest.getMobileNumber()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Failed to create account for mobile number: " + accountRequest.getMobileNumber()));
        }
    }

    private static boolean isAccountActiveByMobileNumber(Account account, String bankName, String mobileNumber) {
        return account.isAccountActive()
                && account.getBankName().equals(bankName)
                && account.getMobileNumber().equals(mobileNumber);
    }

    public AccountsResponse getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accountMapper.mapAccountsToAccountResponse(accounts);
    }

    @Transactional
    public TransferResponse transferFunds(int from, int to, BigDecimal amount) {
        if (from == to)
            throw new IllegalArgumentException("Can't transfer to self account!");

        Account fromAcc = accountRepository.findById(from)
                .orElseThrow(() -> new AccountNotFoundException("From account not found"));

        Account toAcc = accountRepository.findById(to)
                .orElseThrow(() -> new AccountNotFoundException("To account not found"));

        if (fromAcc.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException("Insufficient Balance in " + from + " account");

        fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
        toAcc.setBalance(toAcc.getBalance().add(amount));

        return new TransferResponse(
                "SUCCESS",
                "Your transfer of INR " + amount + " to " + toAcc.getCustomer().getFirstName() + " " + toAcc.getCustomer().getLastName() + " has been successfully completed on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy hh:mm:ss a")));
    }

    public AccountDto getBalanceOf(int accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber + " Account not found"));
        return accountMapper.mapAccountToAccountDto(account);
    }

    @Transactional
    public DeactivateAccountResponse deactivateAccount(int accountNumber, DeactivateAccountRequest deactivateAccountRequest) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber + " Account not found"));

        if (account.isAccountDeactivated()) {
            throw new AccountAlreadyDeactivatedException(accountNumber + " Account already deactivated");
        }

        account.setAccountActive(false);
        account.setDeactivatedMessage(deactivateAccountRequest.getMessage());
        account.setDeactivatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);

        return new DeactivateAccountResponse("DEACTIVATED", saved.getAccountNumber(), saved.getDeactivatedMessage(), saved.getDeactivatedAt());
    }
}
