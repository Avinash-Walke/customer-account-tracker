package com.customer.account.tracker.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"customer_id", "bank_Name"})
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private BigDecimal balance;

    private String currency;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private LocalDateTime accountOpeningDate;

    @Getter(AccessLevel.NONE)
    @Column(name = "account_active", nullable = false)
    private Boolean isAccountActive = true;

    @Column(name = "bank_Name", nullable = false)
    private String bankName;

    @Column(name = "mobile_Number")
    private String mobileNumber;

    @Column(name = "deactivated_message")
    private String deactivatedMessage;

    private LocalDateTime deactivatedAt;

    public Boolean isAccountActive() {
        return isAccountActive;
    }

    public Boolean isAccountDeactivated() {
        return !isAccountActive();
    }

    public void setAccountActive(Boolean accountActive) {
        this.isAccountActive = accountActive;
    }
}