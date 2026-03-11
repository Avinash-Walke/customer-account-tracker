package com.customer.account.tracker.controller;

import com.customer.account.tracker.annotation.JsonFileSource;
import com.customer.account.tracker.dto.CreateAccountRequest;
import com.customer.account.tracker.dto.CustomerResponse;
import com.customer.account.tracker.exception.CustomerNotFoundException;
import com.customer.account.tracker.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomerService customerService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Get customers returns customer response")
    void getCustomers_returnsCustomerResponse() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(new CustomerResponse());

        mockMvc.perform(get("/v1/api/customers"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get customers returns 5xx when service throws runtime exception")
    void getCustomers_returnsServerError_whenServiceThrowsRuntime() throws Exception {
        when(customerService.getAllCustomers()).thenThrow(new RuntimeException());

        mockMvc.perform(get("/v1/api/customers"))
                .andExpect(status().is5xxServerError());
    }

    @ParameterizedTest
    @DisplayName("Update customer returns updated customer for valid payload")
    @JsonFileSource(jsonPath = "json/customer/valid-customer-input.json", returnType = CreateAccountRequest.class)
    void updateCustomer_returnsUpdatedCustomer_forValidPayload(CreateAccountRequest accountRequest) throws Exception {
        when(customerService.updateCustomer(eq(1), any())).thenReturn(new CustomerResponse());

        mockMvc.perform(put("/v1/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @DisplayName("Update customer returns Bad Request for invalid payload")
    @JsonFileSource(jsonPath = "json/customer/invalid-customer-input.json", returnType = CreateAccountRequest.class)
    void updateCustomer_returnsBadRequest_forInvalidPayload(CreateAccountRequest accountRequest) throws Exception {
        mockMvc.perform(put("/v1/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get customer by id returns Not Found when customer missing")
    void getCustomerById_returnsNotFound_whenMissing() throws Exception {
        when(customerService.getCustomerById(5)).thenThrow(new CustomerNotFoundException("not found"));

        mockMvc.perform(get("/v1/api/customers/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get customer by id returns customer response when exists")
    void getCustomerById_returnsCustomerResponse_forExisting() throws Exception {
        when(customerService.getCustomerById(2)).thenReturn(new CustomerResponse());

        mockMvc.perform(get("/v1/api/customers/2"))
                .andExpect(status().isOk());
    }
}
