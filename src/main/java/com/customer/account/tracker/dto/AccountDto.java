package com.customer.account.tracker.dto;

import com.customer.account.tracker.entity.AccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDto {

    private Integer accountNumber;
    private AccountType accountType;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency;
    private CustomerDto customer;

    @JsonFormat(pattern = "dd MMM yyyy, HH:mm a")
    private LocalDateTime accountOpeningDate;
    private Boolean accountActive;
    private String bankName;
    private String mobileNumber;
    private String deactivatedMessage;

    @JsonFormat(pattern = "dd MMM yyyy, HH:mm a")
    private LocalDateTime deactivatedAt;
}