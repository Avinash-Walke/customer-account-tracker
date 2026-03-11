package com.customer.account.tracker.dto;

import com.customer.account.tracker.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateAccountRequest {

    @NotBlank
    @Size(max = 20)
    private String firstName;

    @NotBlank
    @Size(max = 20)
    private String lastName;

    @Email
    @Size(max = 50)
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 10)
    private String mobileNumber;

    @Size(max = 200)
    private String address;

    @NotNull
    private AccountType accountType;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal openingBalance;

    @NotBlank
    private String currency;

    @Past(message = "dob must be in the past")
    private LocalDate dob;

    @NotBlank
    private String bankName;
}
