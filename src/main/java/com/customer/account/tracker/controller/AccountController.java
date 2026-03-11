package com.customer.account.tracker.controller;

import com.customer.account.tracker.dto.*;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Validated
@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        Objects.requireNonNull(accountService, "AccountService should not be null");
        this.accountService = accountService;
    }

    @PostMapping("/v1/api/account")
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest accountRequest) {
        // In real application verify the request body with custom validation or schema validation and header validation
        Account account = accountService.createAccount(accountRequest);
        return ResponseEntity.ok(new CreateAccountResponse("NEW ACCOUNT HAS BEEN CREATED ", account.getAccountNumber()));
    }

    @GetMapping("/v1/api/accounts")
    public ResponseEntity<AccountsResponse> getAllAccounts() {
        AccountsResponse response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v1/api/accounts/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        // In real application verify the request body with custom validation or schema validation and header validation
        TransferResponse transferResponse = accountService.transferFunds(request.getFrom(), request.getTo(), request.getAmount());
        return ResponseEntity.ok(transferResponse);
    }

    @GetMapping("/v1/api/accounts/{accountNumber}/balance")
    public AccountDto getBalance(@PathVariable int accountNumber) {
        return accountService.getBalanceOf(accountNumber);
    }

    @PostMapping("/v1/api/accounts/{accountNumber}/deactivate")
    public ResponseEntity<DeactivateAccountResponse> deactivateAccount(@PathVariable int accountNumber, @Valid @RequestBody DeactivateAccountRequest deactivateAccountRequest) {
        DeactivateAccountResponse response = accountService.deactivateAccount(accountNumber, deactivateAccountRequest);
        return ResponseEntity.ok(response);
    }
}
