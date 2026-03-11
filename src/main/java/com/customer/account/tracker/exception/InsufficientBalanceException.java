package com.customer.account.tracker.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String s) {
        super(s);
    }
}
