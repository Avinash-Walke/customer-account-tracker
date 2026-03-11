package com.customer.account.tracker.controller;

import com.customer.account.tracker.annotation.JsonFileSource;
import com.customer.account.tracker.dto.*;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.entity.AccountType;
import com.customer.account.tracker.exception.AccountAlreadyDeactivatedException;
import com.customer.account.tracker.exception.AccountAlreadyExistsException;
import com.customer.account.tracker.exception.AccountNotFoundException;
import com.customer.account.tracker.exception.InsufficientBalanceException;
import com.customer.account.tracker.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountService accountService;

    ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @DisplayName("Create account returns created account number for valid request")
    @JsonFileSource(jsonPath = "json/account/create-account-valid.json", returnType = CreateAccountRequest.class)
    void createAccount_returnsCreatedAccountNumber_forValidRequest(CreateAccountRequest accountRequest) throws Exception {
        Account account = new Account();
        account.setAccountNumber(123);
        when(accountService.createAccount(any())).thenReturn(account);

        mockMvc.perform(post("/v1/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW ACCOUNT HAS BEEN CREATED "))
                .andExpect(jsonPath("$.accountNumber").value(123));

        verify(accountService).createAccount(any());
    }

    @ParameterizedTest
    @DisplayName("Create account returns Bad Request for invalid request payload")
    @JsonFileSource(jsonPath = "json/account/create-account-invalid.json", returnType = CreateAccountRequest.class)
    void createAccount_returnsBadRequest_forInvalidRequest(CreateAccountRequest accountRequest) throws Exception {
        mockMvc.perform(post("/v1/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Create account returns Conflict when customer already has an account")
    @JsonFileSource(jsonPath = "json/account/create-account-valid.json", returnType = CreateAccountRequest.class)
    void createAccount_returnsConflict_whenAccountAlreadyExists(CreateAccountRequest accountRequest) throws Exception {
        when(accountService.createAccount(any())).thenThrow(new AccountAlreadyExistsException("exists"));

        mockMvc.perform(post("/v1/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @DisplayName("Transfer returns success for valid transfer request")
    @JsonFileSource(jsonPath = "json/account/transfer-valid.json", returnType = TransferRequest.class)
    void transfer_returnsSuccess_forValidTransfer(TransferRequest transferRequest) throws Exception {
        when(accountService.transferFunds(anyInt(), anyInt(), any()))
                .thenReturn(new TransferResponse("SUCCESS", "ok"));

        mockMvc.perform(post("/v1/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists());

        verify(accountService).transferFunds(anyInt(), anyInt(), any());
    }

    @ParameterizedTest
    @DisplayName("Transfer returns Bad Request when insufficient funds")
    @JsonFileSource(jsonPath = "json/account/transfer-valid.json", returnType = TransferRequest.class)
    void transfer_returnsBadRequest_whenInsufficientFunds(TransferRequest transferRequest) throws Exception {
        when(accountService.transferFunds(anyInt(), anyInt(), any()))
                .thenThrow(new InsufficientBalanceException("Insufficient Balance in 2 account"));

        mockMvc.perform(post("/v1/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Transfer returns Bad Request when transferring to self account")
    @JsonFileSource(jsonPath = "json/account/transfer-valid.json", returnType = TransferRequest.class)
    void transfer_returnsBadRequest_whenTransferToSelf(TransferRequest transferRequest) throws Exception {
        when(accountService.transferFunds(anyInt(), anyInt(), any()))
                .thenThrow(new IllegalArgumentException("Can't transfer to self account!"));

        mockMvc.perform(post("/v1/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(jsonPath("$.message").value("Can't transfer to self account!"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Transfer returns Bad Request when from account not found")
    @JsonFileSource(jsonPath = "json/account/transfer-valid.json", returnType = TransferRequest.class)
    void transfer_returnsBadRequest_whenAccountNotFound(TransferRequest transferRequest) throws Exception {
        when(accountService.transferFunds(anyInt(), anyInt(), any()))
                .thenThrow(new AccountNotFoundException("From account not found"));

        mockMvc.perform(post("/v1/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(jsonPath("$.message").value("From account not found"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Get all accounts with customer details when requested")
    @JsonFileSource(jsonPath = "json/account/accounts-response.json", returnType = AccountsResponse.class)
    void getAllAccounts_returnsListOfAccounts(AccountsResponse accountsResponse) throws Exception {
        when(accountService.getAllAccounts()).thenReturn(accountsResponse);

        mockMvc.perform(get("/v1/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts.length()").value(1));
    }

    @Test
    @DisplayName("Get balance returns AccountDto for existing account")
    void getBalance_returnsAccountDto_forExistingAccount() throws Exception {
        AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(5);
        accountDto.setAccountType(AccountType.SAVING_INDIVIDUAL);
        accountDto.setBalance(new BigDecimal("500.00"));

        when(accountService.getBalanceOf(5)).thenReturn(accountDto);

        mockMvc.perform(get("/v1/api/accounts/5/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(5))
                .andExpect(jsonPath("$.balance").value(500.00));
    }

    @Test
    @DisplayName("Get balance returns Bad Request when account not found")
    void getBalance_returnsBadRequest_forNonExistingAccount() throws Exception {
        when(accountService.getBalanceOf(10)).thenThrow(new AccountNotFoundException("not found"));

        mockMvc.perform(get("/v1/api/accounts/10/balance"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get all accounts returns 404 when service throws runtime exception")
    void getAllAccounts_returnsNotFound_whenServiceThrowsRuntime() throws Exception {
        when(accountService.getAllAccounts()).thenThrow(new RuntimeException());

        mockMvc.perform(get("/v1/api/accounts"))
                .andExpect(status().is5xxServerError());
    }

    @ParameterizedTest
    @DisplayName("Deactivate account returns success for valid request")
    @JsonFileSource(jsonPath = "json/account/deactivate-valid.json", returnType = DeactivateAccountRequest.class)
    void deactivateAccount_returnsSuccess_forValidRequest(DeactivateAccountRequest accountRequest) throws Exception {
        DeactivateAccountResponse resp = new DeactivateAccountResponse("DEACTIVATED", 12, accountRequest.getMessage(), null);
        when(accountService.deactivateAccount(eq(12), any(DeactivateAccountRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/v1/api/accounts/12/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEACTIVATED"))
                .andExpect(jsonPath("$.accountNumber").value(12));

        verify(accountService).deactivateAccount(eq(12), any(DeactivateAccountRequest.class));
    }

    @ParameterizedTest
    @DisplayName("Deactivate account returns Bad Request for invalid request payload")
    @JsonFileSource(jsonPath = "json/account/deactivate-invalid.json", returnType = DeactivateAccountRequest.class)
    void deactivateAccount_returnsBadRequest_forInvalidRequest(DeactivateAccountRequest deactivateAccountRequest) throws Exception {
        mockMvc.perform(post("/v1/api/accounts/12/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Deactivate account returns throw account already deactivated")
    @JsonFileSource(jsonPath = "json/account/deactivate-valid.json", returnType = DeactivateAccountRequest.class)
    void deactivateAccount_returnsBadRequest_whenAlreadyDeactivated(DeactivateAccountRequest deactivateAccountRequest) throws Exception {
        when(accountService.deactivateAccount(eq(1), any(DeactivateAccountRequest.class))).thenThrow(new AccountAlreadyDeactivatedException("1 Account already deactivated"));

        mockMvc.perform(post("/v1/api/accounts/1/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Deactivate account returns Bad Request when account missing")
    @JsonFileSource(jsonPath = "json/account/deactivate-valid.json", returnType = DeactivateAccountRequest.class)
    void deactivateAccount_returnsBadRequest_whenMissing(DeactivateAccountRequest deactivateAccountRequest) throws Exception {
        when(accountService.deactivateAccount(eq(3), any(DeactivateAccountRequest.class)))
                .thenThrow(new AccountNotFoundException("not found"));

        mockMvc.perform(post("/v1/api/accounts/3/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateAccountRequest)))
                .andExpect(status().isBadRequest());
    }
}
