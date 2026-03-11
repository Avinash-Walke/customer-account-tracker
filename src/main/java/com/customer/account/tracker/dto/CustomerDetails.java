package com.customer.account.tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDetails {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String address;
    private LocalDate dob;
}
