package com.customer.account.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeactivateAccountRequest {

    @NotBlank
    private String message;
    private String requestedBy;
}

