package com.customer.account.tracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransferRequest {
    @NotNull
    private Integer from;

    @NotNull
    private Integer to;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
