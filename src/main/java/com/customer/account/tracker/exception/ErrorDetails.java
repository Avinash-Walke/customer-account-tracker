package com.customer.account.tracker.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class ErrorDetails {

    private String traceId = UUID.randomUUID().toString();
    private HttpStatus status;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorDetails() {
    }

    public ErrorDetails(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
