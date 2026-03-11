package com.customer.account.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DeactivateAccountResponse {

    private String status;
    private Integer accountNumber;
    private String message;
    private LocalDateTime deactivatedAt;
}

