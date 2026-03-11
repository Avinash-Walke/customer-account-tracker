package com.customer.account.tracker.exception;

public class AccountAlreadyDeactivatedException extends RuntimeException {
    public AccountAlreadyDeactivatedException(String message) {
        super(message);
    }
}

