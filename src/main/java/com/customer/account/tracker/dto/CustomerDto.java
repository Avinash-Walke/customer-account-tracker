package com.customer.account.tracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerDto {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String address;
    private LocalDate dob;
}
