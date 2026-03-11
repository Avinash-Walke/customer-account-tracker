package com.customer.account.tracker.exception;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String msg) {
        super(msg);
    }
}
