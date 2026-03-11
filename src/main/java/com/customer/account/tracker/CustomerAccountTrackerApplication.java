package com.customer.account.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CustomerAccountTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerAccountTrackerApplication.class, args);
    }
}
