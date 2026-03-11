package com.customer.account.tracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferResponse {

    private String status;
    private String message;

    public TransferResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
